package com.algorux.mailer;
import com.algorux.mailer.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class sendMail extends EmailService {
    private final Logger logger = LoggerFactory.getLogger(com.algorux.mailer.sendMail.class);

    @Value("${algorux.server.host}")
    private String serverHost;

    @Override
    public void sendNewMail(String[] to, String subject, String body) {
        super.sendNewMail(to, subject, body);
    }

    @PostMapping({"/sendMail/{email}"})
    String enviarMail(@RequestBody Map<String, Object> datos, @PathVariable String email) {
        this.logger.info("email: " + email);
        datos.keySet().forEach(llave -> {
            if (llave.equals("work_package")) {
                LinkedHashMap data = (LinkedHashMap)datos.get(llave);
                HashMap embedded = (HashMap)data.get("_embedded");
                HashMap author = (HashMap)embedded.get("author");
                HashMap assignee = (HashMap)embedded.get("assignee");
                HashMap status = (HashMap)embedded.get("status");
                HashMap type = (HashMap)embedded.get("type");
                HashMap priority = (HashMap)embedded.get("priority");
                HashMap project = (HashMap)embedded.get("project");
                HashMap links = (HashMap)data.get("_links");
                HashMap description = (HashMap)data.get("description");
                String desc_html = description.get("html").toString();
                if (desc_html.indexOf("*****") != -1)
                    desc_html = desc_html.substring(0,desc_html.indexOf("*****")) + "</p>";
                HashMap customfield6 = (HashMap)links.get("customField6"); //Ubicacion
                HashMap customfield7 = (HashMap)links.get("customField7"); // Id del bien
                HashMap customfield3 = (HashMap)links.get("customField3"); // Solicitante
                HashMap customField9 = (HashMap)links.get("customField9"); // Id del bien 2
                String email_solicitante = "";
                String direccion = "";
                String bien = "";
                List<String> mailto = new ArrayList<>();
                if (customfield6 != null && customfield6.get("title") != null)
                    direccion = customfield6.get("title").toString();
                if (customfield7 != null && customfield7.get("title") != null)
                    bien = customfield7.get("title").toString();
                if (customField9 != null && customField9.get("title") != null)
                    bien = customField9.get("title").toString();
                if (customfield3 != null) {
                    String[] solicitanteFields = customfield3.get("title").toString().split("-");
                    if (solicitanteFields.length > 1 && solicitanteFields[1] != null) {
                        email_solicitante = solicitanteFields[1];
                        if (validateEmail(solicitanteFields[1]))
                            mailto.add(solicitanteFields[1]);
                    }
                }
                String project_name = project.get("identifier").toString();
                String package_id = data.get("id").toString();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                String up_date = "";
                String status_name = status.get("name").toString();
                if (status_name.equals("Cierre")) {
                    up_date = "Fecha de cierre: " + dtf.format(now);
                } else if (status_name.equals("Apertura")) {
                    up_date = "Ticket creado: " + dtf.format(now);
                } else {
                    up_date = "Ticket actualizado: " + dtf.format(now);
                }
                if (validateEmail(email))
                    mailto.add(email);
                mailto.add(author.get("email").toString());
                if (assignee != null)
                    mailto.add(assignee.get("email").toString());
                String ticketNo = package_id;
                if (!desc_html.equals("")) {
                    try {
                        sendNewHTMLmail(mailto.<String>toArray(new String[0]), "Ticket numero: " + ticketNo + " Estado: " + status_name,
                                "<br> Prioridad: " + priority.get("name").toString() +
                                        "<br> Creado por: " + author.get("name").toString() +
                                        "<br> Dirección" + direccion +
                                        "<br> Id y Desc del bien: " + bien + "<br> " +
                                        "<br>" +
                                        up_date +
                                        "<br> Puede ver los detalles en el siguiente enlace: " + serverHost + "projects/" + project_name + "/work_packages/" + package_id + "/activity"+
                                        "<br>" +desc_html


                        );
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    sendNewMail(mailto.<String>toArray(new String[0]), "Ticket numero: " + ticketNo + " Estado: " + status_name,
                            "\n Prioridad: " + priority.get("name").toString() +
                                    "\n Creado por: " + author.get("name").toString() +
                                    "\n Dirección:" + direccion +
                                    "\n Id y Desc del bien: " + bien +
                                    "\n" + up_date +
                                    "\n Puede ver los detalles en el siguiente enlace: " + serverHost + "projects/" + project_name + "/work_packages/" + package_id + "/activity"
                    );
                }
                this.logger.info("Email enviado a:" + mailto);
            } else {
                this.logger.info("llave: {}, valor: {}", llave, datos.get(llave));
            }
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
