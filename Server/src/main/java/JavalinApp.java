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
            ctx.json(operations.serviceGetLogs());
        });

        // POST requests to path /signup will invoke method signup with required parameters and receive its response
        app.post("/signup", ctx -> {
            HashMap<String, String> mapRequest = ctx.bodyAsClass(HashMap.class);
            HashMap<String, String> mapResponse = new HashMap<>();
            AppResponse response = operations.signUp(mapRequest.get("username"), mapRequest.get("password"), new byte[256]);
            mapResponse.put("success", response.getSuccess());
            mapResponse.put("error", response.getError());
            System.out.println(response.getSuccess());
            System.out.println(response.getError());
            ctx.json(mapResponse);
            ctx.status(201);
        });

        app.post("/createalbum", ctx -> {
            HashMap<String, String> mapRequest = ctx.bodyAsClass(HashMap.class);
            HashMap<String, String> mapResponse = new HashMap<>();
            AppResponse response = operations.createAlbum(Integer.valueOf(mapRequest.get("sessionId")), mapRequest.get("username"), mapRequest.get("albumName"), mapRequest.get("sliceURL"));
            mapResponse.put("success", response.getSuccess());
            mapResponse.put("error", response.getError());
            System.out.println(response.getSuccess());
            System.out.println(response.getError());
            ctx.json(mapResponse);
            ctx.status(201);
        });

        app.put("/login", ctx -> {
            HashMap<String, String> mapRequest = ctx.bodyAsClass(HashMap.class);
            HashMap<String, String> mapResponse = new HashMap<>();
            AppResponse response = operations.logIn(mapRequest.get("username"), mapRequest.get("password"));
            mapResponse.put("success", response.getSuccess());
            mapResponse.put("error", response.getError());
            mapResponse.put("sessionId", String.valueOf(response.getSessionId()));
            System.out.println(response.getSuccess());
            System.out.println(response.getError());
            System.out.println(response.getSessionId());
            ctx.json(mapResponse);
            ctx.status(201);
        });

        app.delete("/logout", ctx -> {
            AppRequest request = ctx.bodyAsClass(AppRequest.class);
            ctx.json(operations.logOut(request.getSessionId()));
            ctx.status(200);
        });

        return app;
    }
}
