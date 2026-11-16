package br.com.fintrack.controller;

import br.com.fintrack.dto.CategoriaDTO;
import br.com.fintrack.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@Tag(name="Categorias", description="Gerenciamento das categorias do usuário autenticado")
@SecurityRequirement(name="bearerAuth")
@PreAuthorize("hasRole('USER')")
@ApiResponses({
    @ApiResponse(responseCode="401", description="Token ausente, inválido ou expirado"),
    @ApiResponse(responseCode="403", description="Usuário autenticado não é o proprietário do recurso")
})
public class CategoriaController {
    private final CategoriaService service;
    public CategoriaController(CategoriaService service){this.service=service;}

    @GetMapping
    @Operation(summary="Listar categorias do usuário autenticado")
    @ApiResponse(responseCode="200", description="Categorias do usuário autenticado")
    public List<CategoriaDTO> listar(Authentication auth){return service.listar(auth);}

    @PostMapping
    @Operation(summary="Criar categoria")
    @ApiResponses({
        @ApiResponse(responseCode="201", description="Categoria criada"),
        @ApiResponse(responseCode="400", description="Dados inválidos"),
        @ApiResponse(responseCode="409", description="Já existe categoria com o mesmo nome")
    })
    public ResponseEntity<CategoriaDTO> criar(@Valid @RequestBody CategoriaDTO dto,Authentication auth){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto,auth));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') and @categoriaService.isOwner(authentication, #p0)")
    @Operation(summary="Atualizar categoria pertencente ao usuário")
    @ApiResponses({
        @ApiResponse(responseCode="200", description="Categoria atualizada"),
        @ApiResponse(responseCode="400", description="Dados inválidos"),
        @ApiResponse(responseCode="403", description="Categoria pertence a outro usuário"),
        @ApiResponse(responseCode="404", description="Categoria não encontrada"),
        @ApiResponse(responseCode="409", description="Conflito de nome ou categoria vinculada")
    })
    public CategoriaDTO atualizar(@PathVariable Long id,@Valid @RequestBody CategoriaDTO dto,Authentication auth){
        return service.atualizar(id,dto,auth);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') and @categoriaService.isOwner(authentication, #p0)")
    @Operation(summary="Excluir categoria pertencente ao usuário")
    @ApiResponses({
        @ApiResponse(responseCode="204", description="Categoria excluída"),
        @ApiResponse(responseCode="403", description="Categoria pertence a outro usuário"),
        @ApiResponse(responseCode="404", description="Categoria não encontrada"),
        @ApiResponse(responseCode="409", description="Categoria possui transações vinculadas")
    })
    public ResponseEntity<Void> excluir(@PathVariable Long id,Authentication auth){
        service.excluir(id,auth);return ResponseEntity.noContent().build();
    }
}
