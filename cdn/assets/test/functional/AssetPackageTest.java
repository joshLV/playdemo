package functional;
import org.junit.*;
import play.test.*;
import play.mvc.*;
import play.mvc.Http.*;

public class AssetPackageTest extends FunctionalTest {

    @Test
    public void testMergeOneJsFiles() {
        Response response = GET("/js/test/a.js");
        assertIsOk(response);
        assertContentType("application/javascript", response);
        assertContentMatch("a.js", response);
    }
        
    @Test
    public void testMerge2JsFiles() {
        Response response = GET("/js/test/a.js/test/b.js");
        assertIsOk(response);
        assertContentType("application/javascript", response);
        assertContentMatch("a.js", response);
        assertContentMatch("b.js", response);
    }
    

    @Test
    public void testMerge3JsFiles() {
        Response response = GET("/js/test/a.js/test/b.js/test/c.js");
        assertIsOk(response);
        assertContentType("application/javascript", response);
        assertContentMatch("a.js", response);
        assertContentMatch("b.js", response);
        assertContentMatch("c.js", response);
    }    
    
    @Test
    public void testMergeOneCssFiles() {
        Response response = GET("/css/test/a.css");
        assertIsOk(response);
        assertContentType("text/css", response);
        assertContentMatch("aaaaaa", response);
    }
    
  
    @Test
    public void testMerge2CssFiles() {
        Response response = GET("/css/test/a.css/test/b.css");
        assertIsOk(response);
        assertContentType("text/css", response);
        assertContentMatch("aaaaaa", response);
        assertContentMatch("bbbbbb", response);
    }
    

    @Test
    public void testMerge3CssFiles() {
        Response response = GET("/css/test/a.css/test/b.css/test/c.css");
        assertIsOk(response);
        assertContentType("text/css", response);
        assertContentMatch("aaaaaa", response);
        assertContentMatch("bbbbbb", response);
        assertContentMatch("cccccc", response);
    }    
       
}