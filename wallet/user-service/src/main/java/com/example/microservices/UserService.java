package com.example.microservices;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserCacheRepository userCacheRepository;
    private final KafkaTemplate<String,String> kafkaTemplate;
    private double onboardingAmount;

    public UserService(UserRepository userRepository, UserCacheRepository userCacheRepository,
                       KafkaTemplate<String,String> kafkaTemplate, @Value("${user.onboarding.reward}") double onboardingAmount)
    {
        this.userRepository = userRepository;
        this.userCacheRepository = userCacheRepository;
        this.kafkaTemplate= kafkaTemplate;
        this.onboardingAmount = onboardingAmount;
    }

    public void createUser(UserCreateRequest userCreateRequest) throws JsonProcessingException {
        User user = userCreateRequest.to();
        User savedUser = userRepository.save(user);
        userCacheRepository.save(savedUser);

        //TODO: Need to trigger user creation event so that other service listens to this and adds Rs 10 to user wallet
        // Data that needs to be passed- id, email, nationalId, country, amount, password

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId",savedUser.getId());
        jsonObject.put("name",savedUser.getName());
        jsonObject.put("email",savedUser.getEmail());
        jsonObject.put("nationalId",savedUser.getNationalId());
        jsonObject.put("country", savedUser.getCountry());
        jsonObject.put("amount", onboardingAmount);

        ObjectMapper objectMapper = new ObjectMapper();

        kafkaTemplate.send("USER_CREATE_TOPIC", objectMapper.writeValueAsString(jsonObject));
    }

    public User getUser(int userId)
    {
        User user = userCacheRepository.get(userId);
        if(user == null)
        {
            user = userRepository.findById(userId).orElse(null);
            if(user!=null)
            {
                userCacheRepository.save(user);
            }
        }
        return user;
    }
}
