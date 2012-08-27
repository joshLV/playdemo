<%
String host = request.getServerName();

String url = null;
if (host.contains("localhost") && !host.contains("localhost.")) {
    url = "http://localhost:9101/";
} else {
    url = "http://" + host.substring(0, host.indexOf("."));
    url += ".home." + host.substring(host.indexOf("cas.") + 4);
}
response.sendRedirect(url);
%>
