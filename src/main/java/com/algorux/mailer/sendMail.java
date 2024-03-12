package com.algorux.mailer;
import org.apache.catalina.core.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class sendMail extends EmailService {
    private final  Logger logger = LoggerFactory.getLogger(sendMail.class);
    @Value("${algorux.server.host}")
    private String serverHost;
    @PostMapping ("/sendMail/{email}")
    String enviarMail(@RequestBody Map<String, Object> datos, @PathVariable String email)
    {
        logger.info("email: " + email);
        datos.keySet().forEach(llave ->{
            if (llave.equals("work_package")){
                LinkedHashMap data = (LinkedHashMap) datos.get(llave);
                HashMap embedded = (HashMap) data.get("_embedded"); //_embedded es la llave donde se almacena lo que trae
                HashMap author = (HashMap) embedded.get("author");
                HashMap assignee = (HashMap) embedded.get("assignee");
                HashMap status = (HashMap) embedded.get("status");
                HashMap project = (HashMap) embedded.get("project");
                String project_name = project.get("identifier").toString();
                String package_id = data.get("id").toString();
                List<String> mailto = new ArrayList<String>();
                String status_name = status.get("name").toString();
                mailto.add(author.get("email").toString());
                if (assignee != null)
                    mailto.add(assignee.get("email").toString());
                String ticketNo = package_id;
                this.sendNewMail(mailto.toArray(new String[0]),"Ticket numero: "+ticketNo+" Estado: "+status_name, "Puede ver los detalles en el siguiente enlace: " + serverHost+"projects/"+project_name+"/work_packages/"+package_id+"/activity");
                logger.info( "Email enviado a:" + mailto);


            }
            else
                logger.info("llave: {}, valor: {}", llave, datos.get(llave));
        });
        return "Ok\n";
    }

    String trySend (HashMap<String, String> datos, String fromemail){
        String result = "No se ha podido enviar el correo: ";

        return result;
    }
}
