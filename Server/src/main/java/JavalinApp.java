import io.javalin.Javalin;

import java.util.HashMap;

public class JavalinApp {

    public static final int PORT = 7000;

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

        HashMap<String, String> hash = new HashMap<String, String>();
        hash.put("status", "OK");
        // Requests to the root path will return the JSON { "status" : "OK" }
        app.get("/", ctx -> ctx.json(hash));

        // POST requests to path /signup will invoke method signup with required parameters and receive its response
        app.post("/signup", ctx -> {
            AppRequest request = ctx.bodyAsClass(AppRequest.class);
            ctx.json(operations.signUp(request.getUsername(), request.getPassword(), request.getPublicKey()));
            ctx.status(201);
        });

        app.post("/login", ctx -> {
            AppRequest request = ctx.bodyAsClass(AppRequest.class);
            ctx.json(operations.logIn(request.getUsername(), request.getPassword()));
            ctx.status(201);
        });

        return app;
    }
}
