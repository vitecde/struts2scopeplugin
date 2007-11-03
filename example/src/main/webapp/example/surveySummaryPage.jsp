<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html>
<head>
    <title>Survey</title>
</head>

<body>
Thank you for taking this survey!
<p>First name: <s:property  value="surveyBean.firstName" /></p>
<p>Last name: <s:property value="surveyBean.lastName" /></p>
<p>Age: <s:property value="surveyBean.age" /></p>
<p>Birthdate: <s:property value="surveyBean.birthDate" /></p>
</body>
</html>