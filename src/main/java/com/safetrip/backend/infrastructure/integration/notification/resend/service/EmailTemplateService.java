package com.safetrip.backend.infrastructure.integration.notification.resend.service;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Servicio para procesar plantillas de email
 */
@Service
public class EmailTemplateService {

    /**
     * Procesa una plantilla HTML simple reemplazando variables
     * @param template Plantilla HTML con variables en formato {{variable}}
     * @param variables Mapa de variables y sus valores
     * @return HTML procesado
     */
    public String processTemplate(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            result = result.replace(placeholder, entry.getValue());
        }
        return result;
    }

    public String getOtpTemplate() {
        return """
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
            <html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office" lang="es">
            <head>
            <title></title>
            <meta charset="UTF-8" />
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
            <meta http-equiv="X-UA-Compatible" content="IE=edge" />
            <meta name="x-apple-disable-message-reformatting" content="" />
            <meta content="target-densitydpi=device-dpi" name="viewport" />
            <meta content="true" name="HandheldFriendly" />
            <meta content="width=device-width" name="viewport" />
            <meta name="format-detection" content="telephone=no, date=no, address=no, email=no, url=no" />
            <style type="text/css">
            table {
            border-collapse: separate;
            table-layout: fixed;
            mso-table-lspace: 0pt;
            mso-table-rspace: 0pt
            }
            table td {
            border-collapse: collapse
            }
            .ExternalClass {
            width: 100%
            }
            .ExternalClass,
            .ExternalClass p,
            .ExternalClass span,
            .ExternalClass font,
            .ExternalClass td,
            .ExternalClass div {
            line-height: 100%
            }
            body, a, li, p, h1, h2, h3 {
            -ms-text-size-adjust: 100%;
            -webkit-text-size-adjust: 100%;
            }
            html {
            -webkit-text-size-adjust: none !important
            }
            body {
            min-width: 100%;
            Margin: 0px;
            padding: 0px;
            }
            body, #innerTable {
            -webkit-font-smoothing: antialiased;
            -moz-osx-font-smoothing: grayscale
            }
            #innerTable img+div {
            display: none;
            display: none !important
            }
            img {
            Margin: 0;
            padding: 0;
            -ms-interpolation-mode: bicubic
            }
            h1, h2, h3, p, a {
            line-height: inherit;
            overflow-wrap: normal;
            white-space: normal;
            word-break: break-word
            }
            a {
            text-decoration: none
            }
            h1, h2, h3, p {
            min-width: 100%!important;
            width: 100%!important;
            max-width: 100%!important;
            display: inline-block!important;
            border: 0;
            padding: 0;
            margin: 0
            }
            a[x-apple-data-detectors] {
            color: inherit !important;
            text-decoration: none !important;
            font-size: inherit !important;
            font-family: inherit !important;
            font-weight: inherit !important;
            line-height: inherit !important
            }
            u + #body a {
            color: inherit;
            text-decoration: none;
            font-size: inherit;
            font-family: inherit;
            font-weight: inherit;
            line-height: inherit;
            }
            a[href^="mailto"],
            a[href^="tel"],
            a[href^="sms"] {
            color: inherit;
            text-decoration: none
            }
            .otp-code {
            font-family: 'Courier New', monospace;
            font-size: 42px;
            font-weight: 700;
            letter-spacing: 12px;
            color: #FF4367;
            text-align: center;
            padding: 25px;
            background-color: #F8F9FA;
            border-radius: 8px;
            border: 2px dashed #FF4367;
            margin: 20px 0;
            display: block;
            }
            </style>
            <style type="text/css">
            @media (min-width: 481px) {
            .hd { display: none!important }
            }
            </style>
            <style type="text/css">
            @media (max-width: 480px) {
            .hm { display: none!important }
            }
            </style>
            <style type="text/css">
            @media (max-width: 480px) {
            .t3,.t7{vertical-align:middle!important}.t42,.t47{mso-line-height-alt:0px!important;line-height:0!important;display:none!important}.t43{padding:40px!important;border-radius:0!important}.t8{text-align:left!important}.t7{width:228px!important}.t3{width:287px!important}
            .otp-code {
            font-size: 28px;
            letter-spacing: 6px;
            padding: 20px;
            }
            }
            </style>
            <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;700&amp;display=swap" rel="stylesheet" type="text/css" />
            </head>
            <body id="body" class="t50" style="min-width:100%;Margin:0px;padding:0px;background-color:#FFFFFF;">
            <div class="t49" style="background-color:#FFFFFF;">
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0" align="center">
            <tr>
            <td class="t48" style="font-size:0;line-height:0;mso-line-height-rule:exactly;background-color:#FFFFFF;" valign="top" align="center">
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0" align="center" id="innerTable">
            <tr><td><div class="t42" style="mso-line-height-rule:exactly;mso-line-height-alt:50px;line-height:50px;font-size:1px;display:block;">&nbsp;&nbsp;</div></td></tr>
            <tr><td align="center">
            <table class="t46" role="presentation" cellpadding="0" cellspacing="0" style="Margin-left:auto;Margin-right:auto;">
            <tr>
            <td width="600" class="t45" style="width:600px;">
            <table class="t44" role="presentation" cellpadding="0" cellspacing="0" width="100%" style="width:100%;">
            <tr>
            <td class="t43" style="border:1px solid #EBEBEB;overflow:hidden;background-color:#FFFFFF;padding:44px 42px 32px 42px;border-radius:3px 3px 3px 3px;">
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="width:100% !important;">
            
            <!-- Logo Section -->
            <tr><td align="center">
            <table class="t15" role="presentation" cellpadding="0" cellspacing="0" style="Margin-left:auto;Margin-right:auto;">
            <tr>
            <td width="514" class="t14" style="width:600px;">
            <table class="t13" role="presentation" cellpadding="0" cellspacing="0" width="100%" style="width:100%;">
            <tr>
            <td class="t12">
            <div class="t11" style="width:100%;text-align:left;">
            <div class="t10" style="display:inline-block;">
            <table class="t9" role="presentation" cellpadding="0" cellspacing="0" align="left" valign="middle">
            <tr class="t8">
            <td></td>
            <td class="t3" width="287" valign="middle">
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" class="t2" style="width:287px;">
            <tr>
            <td class="t1">
            <div style="font-size:0px;">
            <img class="t0" style="display:block;border:0;height:auto;width:100%;Margin:0;max-width:100%;" width="287" height="149.10546875" alt="" src="https://77f55f77-ca23-49a2-a8a1-037c6fafc255.b-cdn.net/e/32001ed5-7ea0-4ca3-bc31-22d01f05d916/b425de0a-23b6-45e0-869f-21ca03a6b094.png"/>
            </div>
            </td>
            </tr>
            </table>
            </td>
            <td class="t7" width="228" valign="middle">
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" class="t6" style="width:228px;">
            <tr>
            <td class="t5">
            <div style="font-size:0px;">
            <img class="t4" style="display:block;border:0;height:auto;width:100%;Margin:0;max-width:100%;" width="228" height="211.42857142857142" alt="" src="https://77f55f77-ca23-49a2-a8a1-037c6fafc255.b-cdn.net/e/32001ed5-7ea0-4ca3-bc31-22d01f05d916/d1835bd9-cc59-49db-8b98-3a75abb5d3a8.png"/>
            </div>
            </td>
            </tr>
            </table>
            </td>
            <td></td>
            </tr>
            </table>
            </div>
            </div>
            </td>
            </tr>
            </table>
            </td>
            </tr>
            </table>
            </td></tr>
            
            <!-- Greeting -->
            <tr><td align="center">
            <table class="t20" role="presentation" cellpadding="0" cellspacing="0" style="Margin-left:auto;Margin-right:auto;">
            <tr>
            <td width="514" class="t19" style="width:600px;">
            <table class="t18" role="presentation" cellpadding="0" cellspacing="0" width="100%" style="width:100%;">
            <tr>
            <td class="t17" style="padding:0 0 18px 0;">
            <h1 class="t16" style="margin:0;Margin:0;font-family:Poppins,BlinkMacSystemFont,Segoe UI,Helvetica Neue,Arial,sans-serif;line-height:28px;font-weight:700;font-style:normal;font-size:24px;text-decoration:none;text-transform:none;letter-spacing:-1px;direction:ltr;color:#141414;text-align:left;mso-line-height-rule:exactly;mso-text-raise:1px;">¡Hola {{userName}}!</h1>
            </td>
            </tr>
            </table>
            </td>
            </tr>
            </table>
            </td></tr>
            
            <!-- Title -->
            <tr><td align="center">
            <table class="t25" role="presentation" cellpadding="0" cellspacing="0" style="Margin-left:auto;Margin-right:auto;">
            <tr>
            <td width="514" class="t24" style="width:600px;">
            <table class="t23" role="presentation" cellpadding="0" cellspacing="0" width="100%" style="width:100%;">
            <tr>
            <td class="t22" style="border-bottom:1px solid #EFF1F4;padding:0 0 18px 0;">
            <h1 class="t21" style="margin:0;Margin:0;font-family:Poppins,BlinkMacSystemFont,Segoe UI,Helvetica Neue,Arial,sans-serif;line-height:28px;font-weight:700;font-style:normal;font-size:24px;text-decoration:none;text-transform:none;letter-spacing:-1px;direction:ltr;color:#FF4367;text-align:left;mso-line-height-rule:exactly;mso-text-raise:1px;">Tu código de acceso es: {{otpCode}}</h1>
            </td>
            </tr>
            </table>
            </td>
            </tr>
            </table>
            </td></tr>

            <tr><td><div style="mso-line-height-rule:exactly;mso-line-height-alt:18px;line-height:18px;font-size:1px;display:block;">&nbsp;&nbsp;</div></td></tr>
            
            <!-- Description -->
            <tr><td align="center">
            <table class="t31" role="presentation" cellpadding="0" cellspacing="0" style="Margin-left:auto;Margin-right:auto;">
            <tr>
            <td width="514" class="t30" style="width:600px;">
            <table class="t29" role="presentation" cellpadding="0" cellspacing="0" width="100%" style="width:100%;">
            <tr>
            <td class="t28">
            <p class="t27" style="margin:0;Margin:0;font-family:Poppins,BlinkMacSystemFont,Segoe UI,Helvetica Neue,Arial,sans-serif;line-height:25px;font-weight:400;font-style:normal;font-size:15px;text-decoration:none;text-transform:none;letter-spacing:-0.1px;direction:ltr;color:#141414;text-align:left;mso-line-height-rule:exactly;mso-text-raise:3px;">Caduca en {{expiryMinutes}} minutos. Por seguridad, no lo compartas con nadie.</p>
            </td>
            </tr>
            </table>
            </td>
            </tr>
            </table>
            </td></tr>
            
            <!-- Footer -->
            <tr><td align="center">
            <table class="t36" role="presentation" cellpadding="0" cellspacing="0" style="Margin-left:auto;Margin-right:auto;">
            <tr>
            <td width="514" class="t35" style="width:600px;">
            <table class="t34" role="presentation" cellpadding="0" cellspacing="0" width="100%" style="width:100%;">
            <tr>
            <td class="t33">
            <p class="t32" style="margin:0;Margin:0;font-family:Poppins,BlinkMacSystemFont,Segoe UI,Helvetica Neue,Arial,sans-serif;line-height:25px;font-weight:400;font-style:normal;font-size:15px;text-decoration:none;text-transform:none;letter-spacing:-0.1px;direction:ltr;color:#141414;text-align:left;mso-line-height-rule:exactly;mso-text-raise:3px;">Gracias,&nbsp;</p>
            </td>
            </tr>
            </table>
            </td>
            </tr>
            </table>
            </td></tr>
            
            <tr><td align="center">
            <table class="t41" role="presentation" cellpadding="0" cellspacing="0" style="Margin-left:auto;Margin-right:auto;">
            <tr>
            <td width="514" class="t40" style="width:600px;">
            <table class="t39" role="presentation" cellpadding="0" cellspacing="0" width="100%" style="width:100%;">
            <tr>
            <td class="t38">
            <p class="t37" style="margin:0;Margin:0;font-family:Poppins,BlinkMacSystemFont,Segoe UI,Helvetica Neue,Arial,sans-serif;line-height:25px;font-weight:400;font-style:normal;font-size:15px;text-decoration:none;text-transform:none;letter-spacing:-0.1px;direction:ltr;color:#141414;text-align:left;mso-line-height-rule:exactly;mso-text-raise:3px;">El equipo de SafeTrip</p>
            </td>
            </tr>
            </table>
            </td>
            </tr>
            </table>
            </td></tr>
            
            </table>
            </td>
            </tr>
            </table>
            </td>
            </tr>
            </table>
            </td></tr>
            <tr><td><div class="t47" style="mso-line-height-rule:exactly;mso-line-height-alt:50px;line-height:50px;font-size:1px;display:block;">&nbsp;&nbsp;</div></td></tr>
            </table>
            </td>
            </tr>
            </table>
            </div>
            <div class="gmail-fix" style="display: none; white-space: nowrap; font: 15px courier; line-height: 0;">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;</div>
            </body>
            </html>
            """;
    }
    /**
     * Obtiene una plantilla de bienvenida
     */
    public String getWelcomeTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .footer { text-align: center; padding: 10px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>¡Bienvenido {{name}}!</h1>
                    </div>
                    <div class="content">
                        <p>Hola {{name}},</p>
                        <p>Gracias por registrarte en nuestra plataforma.</p>
                        <p>Estamos emocionados de tenerte con nosotros.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2025 Tu Empresa. Todos los derechos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }

    /**
     * Obtiene una plantilla de restablecimiento de contraseña
     */
    public String getPasswordResetTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .button { 
                        display: inline-block; 
                        padding: 12px 24px; 
                        background-color: #2196F3; 
                        color: white; 
                        text-decoration: none; 
                        border-radius: 4px; 
                        margin: 20px 0;
                    }
                    .footer { text-align: center; padding: 10px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Restablecer Contraseña</h1>
                    </div>
                    <div class="content">
                        <p>Hola {{name}},</p>
                        <p>Recibimos una solicitud para restablecer tu contraseña.</p>
                        <p>Haz clic en el siguiente botón para continuar:</p>
                        <a href="{{resetLink}}" class="button">Restablecer Contraseña</a>
                        <p>Si no solicitaste este cambio, puedes ignorar este mensaje.</p>
                        <p>Este enlace expirará en 24 horas.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2025 Tu Empresa. Todos los derechos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}