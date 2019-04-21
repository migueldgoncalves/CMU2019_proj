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

        app.get("/users", ctx -> {
            HashMap<String, String> mapRequest = ctx.bodyAsClass(HashMap.class);
            HashMap<String, String> mapResponse = operations.serviceGetUsers(Integer.valueOf(mapRequest.get("sessionId")), mapRequest.get("username"));
            System.out.println("HTTP success: " + mapResponse.get("success"));
            System.out.println("HTTP error: " + mapResponse.get("error"));
            System.out.println("HTTP size: " + mapResponse.get("size"));
            String[] users = mapResponse.get("users").split(",");
            for(int i=0; i<users.length; i++) {
                System.out.println("HTTP user" + i + " has name: " + mapResponse.get(users[i]));
            }
            ctx.json(mapResponse);
        });

        app.get("/useralbums", ctx -> {
            HashMap<String, String> mapRequest = ctx.bodyAsClass(HashMap.class);
            HashMap<String, String> mapResponse = operations.serviceGetUserAlbums(Integer.valueOf(mapRequest.get("sessionId")), mapRequest.get("username"));
            System.out.println("HTTP success: " + mapResponse.get("success"));
            System.out.println("HTTP error: " + mapResponse.get("error"));
            System.out.println("HTTP size: " + mapResponse.get("size"));
            System.out.println("HTTP albums: " + mapResponse.get("albums"));
            String[] albums = mapResponse.get("albums").split(",");
            for(int i=0; i<albums.length; i++) {
                System.out.println("HTTP album" + i + " has id: " + albums[i]);
                System.out.println("HTTP album" + i + " has name: " + mapResponse.get(String.valueOf(albums[i])));
            }
            ctx.json(mapResponse);
        });

        app.get("/album", ctx -> {
            HashMap<String, String> mapRequest = ctx.bodyAsClass(HashMap.class);
            HashMap<String, String> mapResponse = operations.viewAlbum(Integer.valueOf(mapRequest.get("sessionId")), mapRequest.get("username"), Integer.valueOf(mapRequest.get("albumId")));
            System.out.println("HTTP success: " + mapResponse.get("success"));
            System.out.println("HTTP error: " + mapResponse.get("error"));
            System.out.println("HTTP size: " + mapResponse.get("size"));
            System.out.println("HTTP album id: " + mapResponse.get("id"));
            System.out.println("HTTP album name: " + mapResponse.get("name"));
            System.out.println("HTTP users: " + mapResponse.get("users"));
            String[] users = mapResponse.get("users").split(",");
            for(int i=0; i<users.length; i++) {
                System.out.println("HTTP album user " + users[i] + " has slice URL " + mapResponse.get(users[i]));
            }
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

        app.put("/adduser", ctx -> {
            HashMap<String, String> mapRequest = ctx.bodyAsClass(HashMap.class);
            HashMap<String, String> mapResponse = operations.serviceAddUserToAlbum(Integer.valueOf(mapRequest.get("sessionId")), mapRequest.get("username"), Integer.valueOf(mapRequest.get("albumId")), mapRequest.get("usernameToAdd"));
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
