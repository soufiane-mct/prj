package com.aliboo.book.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

//hd l file howa li ykhlina n connectiw hd back end m3a swagger o ykhdm lina documentation dyal l gae api li drna f project o li aysehl lina linkiwh mea frontend
// data dylk libghai teati 3antari9 openapi o ktnf3 la kno nas khrin khdamin f projrct dylh katshl elihom bhd data
//dir start l spring o dkhl l http://localhost:8081/api/v1/swagger-ui.html
@OpenAPIDefinition(
        info = @Info (
                contact = @Contact(
                        name = "Soufiane",
                        email = "soufianemechta45@gmail.com",
                        url = "https://soufiane.com"
                ),
                description = "OpenApi documentation for Spring security",
                title = "",
                version = "1.0",
                license = @License(
                        name = "",
                        url = ""
                ),//lkn endk shi license diro hna bsh ykon endk he9 f site web dylk
                termsOfService = ""
        ),
        servers = {
                @Server(
                        description = "Local ENV",
                        url = "http://localhost:8081/api/v1" //link li khdm fih local
                ),
                @Server(
                        description = "PROD ENV",
                        url = "http://soufiane.com"
                )
        },
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"
                ) //hdi drr d security bsh f kla controller ykhdm bhd security
        }
)
@SecurityScheme(
        name = "bearerAuth", //drr tkn nfs name d SecurityRequirement
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP, //ykon security fl http
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER //ndiro liha include fl header

)
public class OpenApiConfig {
}
