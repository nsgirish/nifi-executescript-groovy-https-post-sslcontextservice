
# HTTPS POST JSON request via NIFI ExecuteScript groovy processor referencing a existing SSLContext controller service 

## Summary

Apache NIFI provides InovkeHttp processor to make HTTP / HTTPS requests. But if there are some customizations required while making the HTTP(S) request such as disabling hostname verification, InvokeHttp processor doesn't support the same. 

So in those times the same functionality can be implemented with additional features via groovy scripting as illustrated in https_json_post.groovy. To make a https request we need a SSLContextFactory object. NIFI InvokeHttp processor uses thes controller service SSLContextService to provide the SSLContextFactory object. Even though SSLContextFactory object can be custom created via groovy code, it is very convenient to use a existing object for all the HTTPS requests as any changes to SSL certificate details just need to updated in the Controller service and all the ExecuteScript components automatically get the update as they resuse the same SSLContext controller service 

The groovy code snippet given in the repo shows how to use a existing SSLContext controller service while making a HTTPS post JSON request. So needless to say, same logic can be followed for other HTTP methods 
