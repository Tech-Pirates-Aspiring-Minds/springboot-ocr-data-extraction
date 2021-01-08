<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="en">
  <head>
      <meta charset="utf-8">
      <title>Log in with your account</title>

      <link href="${contextPath}/resources/css/bootstrap.min.css" rel="stylesheet">
      <link href="${contextPath}/resources/css/common.css" rel="stylesheet">
  <script>
  function recaptchaCallback() 
  {
	  $('#submitBt').removeAttr('disabled');
  }
  </script>
  </head>

  <body>

    <div class="container">
     <!-- <form method="post" action="${contextPath}/login" class="form-signin">  --> 
      <form:form method="POST" action="${contextPath}/login" modelAttribute="userForm" class="form-signin">
        <h2 class="form-heading">Log in</h2>

            <span>${message}</span>
        <spring:bind path="username">
                <div class="form-group ${status.error ? 'has-error' : ''}">
                    <form:input type="text" path="username" class="form-control" placeholder="Username" 
		    autofocus="true" required="true"></form:input>
                    <form:errors path="username"></form:errors>
                </div>
            </spring:bind>

            <spring:bind path="password">
                <div class="form-group ${status.error ? 'has-error' : ''}">
                    <form:input type="password" path="password" class="form-control" placeholder="Password" required="true"></form:input>
                    <form:errors path="password"></form:errors>
                </div>
            </spring:bind> 
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
			<br>
			<div class="g-recaptcha" data-sitekey="6LenFyAaAAAAAC34WXAq_zJtuxIQI-ERjlmn3mTH" class="form-control" data-callback="recaptchaCallback"></div>
	        <span>${error}</span> 
            <button id="submitBt" class="btn btn-lg btn-primary btn-block" type="submit" disabled="disabled">Log In</button>
            <h4 class="text-center"><a href="${contextPath}/registration">Create an account</a></h4>
        
      </form:form>
    </div>

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
    <script src="${contextPath}/resources/js/bootstrap.min.js"></script>
    <script src='https://www.google.com/recaptcha/api.js'></script>
  </body>
</html>
