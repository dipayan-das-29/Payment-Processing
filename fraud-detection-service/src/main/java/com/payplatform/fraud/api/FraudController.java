package com.payplatform.fraud.api;

import com.payplatform.events.PaymentInitiatedEvent;
import com.payplatform.fraud.application.FraudAssessmentResponse;
import com.payplatform.fraud.application.FraudRuleEngine;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/fraud")
public class FraudController {
    private final FraudRuleEngine fraudRuleEngine;

    public FraudController(FraudRuleEngine fraudRuleEngine) {
        this.fraudRuleEngine = fraudRuleEngine;
    }

    @PostMapping("/evaluate")
    public FraudAssessmentResponse evaluate(@RequestBody PaymentInitiatedEvent event) {
        return fraudRuleEngine.evaluate(event);
    }
}
