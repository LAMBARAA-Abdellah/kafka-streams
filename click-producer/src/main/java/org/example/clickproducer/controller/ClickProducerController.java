package org.example.clickproducer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/click")
public class ClickProducerController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // Affiche la page HTML avec le bouton
    @GetMapping
    public String showClickPage() {
        return """
        <html>
          <head>
            <meta charset="UTF-8">
            <title>Click App</title>
            <style>
              body {
                font-family: Arial, sans-serif;
                display: flex;
                justify-content: center;
                align-items: center;
                height: 100vh;
                background-color: #e8f0fe;
              }
              .box {
                background: white;
                padding: 40px;
                border-radius: 10px;
                box-shadow: 0 0 15px rgba(0,0,0,0.2);
                text-align: center;
              }
              .btn {
                padding: 10px 25px;
                font-size: 18px;
                background-color: #4CAF50;
                color: white;
                border: none;
                border-radius: 5px;
                cursor: pointer;
              }
              .btn:hover {
                background-color: #45a049;
              }
              .message {
                margin-top: 20px;
                font-size: 18px;
                color: green;
              }
            </style>
          </head>
          <body>
            <div class="box">
              <button class="btn" onclick="sendClick()">Cliquez ici</button>
              <div class="message" id="msg"></div>
            </div>

            <script>
              function sendClick() {
                fetch('/click/send')
                  .then(response => response.text())
                  .then(data => {
                    document.getElementById('msg').innerText = data;
                  })
                  .catch(err => {
                    document.getElementById('msg').innerText = "❌ Erreur lors de l'envoi.";
                  });
              }
            </script>
          </body>
        </html>
        """;
    }

    // Endpoint qui envoie le message Kafka
    @GetMapping("/send")
    public String sendClickKafka() {
        kafkaTemplate.send("clicks", "abdel", "click");
        return "✅ Click envoyé !";
    }
}
