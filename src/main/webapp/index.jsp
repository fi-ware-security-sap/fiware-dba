<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>FI-WARE DB Anonymizer front-end</title>


<script type="text/javascript">
function defaultStaticFormToViewParameterUrlRedirect( form_id , suffixString ) {
    var form=document.getElementById( form_id );
    var action=form.baseURI + form.action;
 
    var gid=document.getElementById( 'gid' );
    
    action += suffixString;
    
    if ((gid)&&(form_id == 'form_result'))  
    {
        action += escape( gid.value );
    } 

    form.elements.length = 0 ;
    form.elements = null ;
    
    var xmlhttp=new XMLHttpRequest();
    
    
    
    
    var ifrm = document.getElementById('ifrm');
    
    ifrm.src = action;
    
    // mandatory! we need to block the actual HTML FORM action 
    
    return false;
}

</script>


</head>

<body>

<img alt="fi-ware logo" src="fiware-image.png">
<h1>DB Anonymizer HTML front-end</h1>

	<form method="post" enctype="multipart/form-data"  action="services/DBA/evaluatePolicy" 
		name="form_post" id="form_post" target="ifrm">
		<label for="policyFile">Policy File:</label> <input type="file"
			name="policyFile" id="policyFile"
			 /> <br />
		<label for="dbDump">Zipped SQL DB dump (MySQL):</label> <input type="file" name="dbDump"
			id="dbDump" />
		<br /> 
		<!--   <input type="text" name="maxRisk" id="maxRisk" value="0.988" /> -->
		<input type="submit" name="submit" value="Submit New Request" />
	</form>
	<br/>
	<!-- 
	<form id="form_result" name="form_result" action="" method="get" onsubmit="return defaultStaticFormToViewParameterUrlRedirect( 'form_result', 'services/DBA/getPolicyResult/' )">
		<label for="gid">Received Request ID: </label> <input type="text" name="gid" id="gid" /> 
		<input type="submit" name="submit" value="Get Result" />
	</form>
	 -->
 	<form id="form_result" name="form_result" action="services/DBA/getPolicyResult" method="get" target="ifrm">
		<label for="gid">Received Request ID: </label> <input type="text" name="gid" id="gid" /> 
		<input type="submit" name="submit" value="Get Result" />
	</form>	 
	<b>Console:</b><br/>
	<iframe name="ifrm" id="ifrm" width="400"></iframe>
	
</body>

</html>