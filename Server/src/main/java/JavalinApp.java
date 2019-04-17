import io.javalin.Javalin;

import java.util.HashMap;

public class JavalinApp {

    public static final int PORT = 8080;

    public JavalinApp() {

    }

    public Javalin init() {

        Operations operations = Operations.getServer(); //Get server class

        Javalin app = Javalin.create().start(PORT);

        app.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(400);
            ctx.result("Invalid request");
        });

        app.error(404, ctx -> {
            ctx.result("This link does not exist");
        });

        HashMap<String, String> hashRoot = new HashMap<String, String>();
        hashRoot.put("status", "OK");
        // Requests to the root path will return the JSON { "status" : "OK" }
        app.get("/", ctx -> ctx.json(hashRoot));

        app.get("/logs", ctx -> {
            HashMap<String, String> mapResponse = operations.serviceGetLogs();
            System.out.println("HTTP success: " + mapResponse.get("success"));
            System.out.println("HTTP error: " + mapResponse.get("error"));
            System.out.println("HTTP logs: " + mapResponse.get("logs"));
            ctx.json(mapResponse);
        });

        // POST requests to path /signup will invoke method signup with required parameters and receive its response
        app.post("/signup", ctx -> {
            HashMap<String, String> mapRequest = ctx.bodyAsClass(HashMap.class);
            HashMap<String, String> mapResponse = operations.signUp(mapRequest.get("username"), mapRequest.get("password"));
            System.out.println("HTTP success: " + mapResponse.get("success"));
            System.out.println("HTTP error: " + mapResponse.get("error"));
            ctx.json(mapResponse);
            ctx.status(201);
        });

        app.post("/createalbum", ctx -> {
            HashMap<String, String> mapRequest = ctx.bodyAsClass(HashMap.class);
            HashMap<String, String> mapResponse = operations.createAlbum(Integer.valueOf(mapRequest.get("sessionId")), mapRequest.get("username"), mapRequest.get("albumName"));
            System.out.println("HTTP success: " + mapResponse.get("success"));
            System.out.println("HTTP error: " + mapResponse.get("error"));
            ctx.json(mapResponse);
            ctx.status(201);
        });

        app.put("/login", ctx -> {
            HashMap<String, String> mapRequest = ctx.bodyAsClass(HashMap.class);
            HashMap<String, String> mapResponse = operations.logIn(mapRequest.get("username"), mapRequest.get("password"));
            System.out.println("HTTP success: " + mapResponse.get("success"));
            System.out.println("HTTP error: " + mapResponse.get("error"));
            System.out.println("HTTP session id:" + mapResponse.get("sessionId"));
            ctx.json(mapResponse);
            ctx.status(201);
        });

        app.put("/seturl", ctx -> {
            HashMap<String, String> mapRequest = ctx.bodyAsClass(HashMap.class);
            HashMap<String, String> mapResponse = operations.setSliceURL(Integer.valueOf(mapRequest.get("sessionId")), mapRequest.get("username"), mapRequest.get("URL"), Integer.valueOf(mapRequest.get("albumId")));
            System.out.println("HTTP success: " + mapResponse.get("success"));
            System.out.println("HTTP error: " + mapResponse.get("error"));
            ctx.json(mapResponse);
            ctx.status(201);
        });

        app.delete("/logout", ctx -> {
            HashMap<String, String> mapRequest = ctx.bodyAsClass(HashMap.class);
            HashMap<String, String> mapResponse = operations.logOut(Integer.valueOf(mapRequest.get("sessionId")));
            System.out.println("HTTP success: " + mapResponse.get("success"));
            System.out.println("HTTP error: " + mapResponse.get("error"));
            ctx.json(mapResponse);
            ctx.status(200);
        });

        return app;
    }
}
