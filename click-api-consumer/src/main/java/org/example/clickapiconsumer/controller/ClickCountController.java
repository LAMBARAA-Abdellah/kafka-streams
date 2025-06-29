package org.example.clickapiconsumer.controller;

import org.example.clickapiconsumer.service.KafkaConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clicks")
public class ClickCountController {

    private final KafkaConsumerService service;

    public ClickCountController(KafkaConsumerService service) {
        this.service = service;
    }

    @GetMapping("/count")
    public String getClickCount() {
        long count = service.getCount();
        return """
    <html>
      <head>
        <style>
          body {
            font-family: Arial, sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            background-color: #f2f2f2;
          }
          .box {
            background-color: #fff;
            padding: 40px;
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
            text-align: center;
          }
          h1 {
            color: #333;
          }
        </style>
      </head>
      <body>
        <div class="box">
          <h1>Nombre total de clics : %d</h1>
        </div>
      </body>
    </html>
    """.formatted(count);

    }
}
