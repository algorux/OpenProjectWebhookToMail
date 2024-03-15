package com.algorux.mailer;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.apache.catalina.core.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.regex.Pattern;

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
                HashMap type = (HashMap) embedded.get("type");
                HashMap priority = (HashMap) embedded.get("priority");
                HashMap project = (HashMap) embedded.get("project");
                HashMap links = (HashMap) data.get("_links");
                HashMap description = (HashMap) data.get("description");
                String desc_html = description.get("html").toString();
                HashMap customfield6 = (HashMap)  links.get("customField6");
                HashMap customfield7 = (HashMap) links.get("customField7");
                String direccion = "";
                String bien = "";
                if (customfield6 != null)
                    direccion = customfield6.get("title").toString();
                if (customfield7 != null)
                    bien = customfield7.get("title").toString();
                String project_name = project.get("identifier").toString();
                String package_id = data.get("id").toString();
                List<String> mailto = new ArrayList<String>();
                String status_name = status.get("name").toString();
                if (validateEmail(email))
                    mailto.add(email);
                mailto.add(author.get("email").toString());
                if (assignee != null)
                    mailto.add(assignee.get("email").toString());

                String ticketNo = package_id;
                if (!desc_html.equals("")) {
                    try {
                        this.sendNewHTMLmail(mailto.toArray(new String[0]), "Ticket numero: " + ticketNo + " Estado: " + status_name, "Ticket creado: " + type.get("createdAt").toString()
                                + "<br> Prioridad: " + priority.get("name").toString()
                                + "<br> Creado por: " + author.get("name").toString()
                                + "<br> Dirección: " + direccion
                                + "<br> Id y Desc del bien: " + bien
                                + "<br> Puede ver los detalles en el siguiente enlace: " + serverHost + "projects/" + project_name + "/work_packages/" + package_id + "/activity"
                                + desc_html
                        );
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    this.sendNewMail(mailto.toArray(new String[0]), "Ticket numero: " + ticketNo + " Estado: " + status_name, "Ticket creado: " + type.get("createdAt").toString()
                            + "\n Prioridad: " + priority.get("name").toString()
                            + "\n Creado por: " + author.get("name").toString()
                            + "\n Dirección: " + direccion
                            + "\n Id y Desc del bien: " + bien
                            + "\n Puede ver los detalles en el siguiente enlace: " + serverHost + "projects/" + project_name + "/work_packages/" + package_id + "/activity");

                }


                logger.info( "Email enviado a:" + mailto);
                //logger.info(links.toString());

            }
            else
                logger.info("llave: {}, valor: {}", llave, datos.get(llave));
        });
        return "Ok\n";
    }

    public static boolean validateEmail(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

}
