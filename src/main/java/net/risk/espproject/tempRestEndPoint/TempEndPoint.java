package net.risk.espproject.tempRestEndPoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.shaded.gson.JsonObject;
import net.risk.espproject.repository.impl.JwksApiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1")
public class TempEndPoint {
    @Autowired
    private JwksApiRepository jwksApiRepository;

    String useCase = "AccAuth";

    @GetMapping(value = "/getAll", produces = "application/json")
    public ResponseEntity<String> getAllData() throws JsonProcessingException {
        String result = jwksApiRepository.getAllMetaData();
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/getPublicKey", produces = "application/json")
    public ResponseEntity<String> getPublicKey()throws JsonProcessingException {
        JsonObject result = jwksApiRepository.getPublicKey(useCase);
        return ResponseEntity.ok(result.toString());
    }

    @GetMapping(value = "/getPrivateKey", produces = "application/json")
    public ResponseEntity<String> getPrivateKey()throws JsonProcessingException {
        var result = jwksApiRepository.getPrivateKey(useCase);
        return ResponseEntity.ok(result.toString());
    }

}
