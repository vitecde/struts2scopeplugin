<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="ww" uri="/webwork" %>

<html>
<head>
    <title>Survey</title>
</head>

<body>
<h3>Welcome to a sample survey</h3>
<p>
	First Name: <ww:property value="surveyBean.firstName"/><br>
	Last Name: <ww:property value="surveyBean.lastName"/><br>
	Age: <ww:property value="surveyBean.age"/><br>
	Test String: <ww:property value="testString"/><br>
</p>
</body>
</html>