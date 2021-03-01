import org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets
import java.net.ssl.*;
import java.io.*;
import java.net.URL;
import org.apache.nifi.ssl.SSLContextService;
import org.apache.nifi.ssl.SSLContextService.ClientAuth;
import org.apache.nifi.controller.ControllerService;

//get flowfile of previous processor
def flowFile = session.get()
if(!flowFile) return
