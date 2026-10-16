package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints")
@Tag(name = "Blueprints", description = "Operaciones sobre planos (blueprints)")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    // GET /api/v1/blueprints
    @Operation(summary = "Listar todos los blueprints")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        return ResponseEntity.ok(ApiResponse.of(200, "execute ok", services.getAllBlueprints()));
    }

    // GET /api/v1/blueprints/{author}
    @Operation(summary = "Listar los blueprints de un autor")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "El autor no tiene blueprints",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponse<Set<Blueprint>>> byAuthor(
            @Parameter(description = "Autor", example = "john") @PathVariable String author) {
        try {
            return ResponseEntity.ok(ApiResponse.of(200, "execute ok", services.getBlueprintsByAuthor(author)));
        } catch (BlueprintNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // GET /api/v1/blueprints/{author}/{bpname}
    @Operation(summary = "Consultar un blueprint por autor y nombre")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResponse<Blueprint>> byAuthorAndName(
            @Parameter(description = "Autor", example = "john") @PathVariable String author,
            @Parameter(description = "Nombre del blueprint", example = "house") @PathVariable String bpname) {
        try {
            return ResponseEntity.ok(ApiResponse.of(200, "execute ok", services.getBlueprint(author, bpname)));
        } catch (BlueprintNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // POST /api/v1/blueprints
    @Operation(summary = "Crear un nuevo blueprint")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Blueprint creado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos o blueprint ya existente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Blueprint>> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.of(201, "blueprint created", bp));
        } catch (BlueprintPersistenceException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // PUT /api/v1/blueprints/{author}/{bpname}/points
    @Operation(summary = "Agregar un punto a un blueprint existente")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Punto agregado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<ApiResponse<Blueprint>> addPoint(
            @Parameter(description = "Autor", example = "john") @PathVariable String author,
            @Parameter(description = "Nombre del blueprint", example = "house") @PathVariable String bpname,
            @Valid @RequestBody PointRequest p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(ApiResponse.of(202, "point added", services.getBlueprint(author, bpname)));
        } catch (BlueprintNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiResponse.of(status.value(), message, null));
    }

    @Schema(description = "Datos para crear un blueprint")
    public record NewBlueprintRequest(
            @Schema(example = "john") @NotBlank String author,
            @Schema(example = "kitchen") @NotBlank String name,
            @Valid java.util.List<Point> points
    ) { }

    @Schema(description = "Punto a agregar")
    public record PointRequest(
            @Schema(example = "3") @NotNull Integer x,
            @Schema(example = "3") @NotNull Integer y
    ) { }
}
