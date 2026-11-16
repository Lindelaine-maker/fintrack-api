package br.com.fintrack.controller;
import br.com.fintrack.dto.CategoriaDTO;
import br.com.fintrack.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/categorias") @Tag(name="Categorias")
public class CategoriaController {
    private final CategoriaService service;
    public CategoriaController(CategoriaService service){this.service=service;}
    @GetMapping @Operation(summary="Listar categorias do usuário autenticado")
    public List<CategoriaDTO> listar(Authentication auth){return service.listar(auth);}
    @PostMapping @Operation(summary="Criar categoria")
    public ResponseEntity<CategoriaDTO> criar(@Valid @RequestBody CategoriaDTO dto,Authentication auth){return ResponseEntity.status(201).body(service.criar(dto,auth));}
    @PutMapping("/{id}") @Operation(summary="Atualizar categoria")
    public CategoriaDTO atualizar(@PathVariable Long id,@Valid @RequestBody CategoriaDTO dto,Authentication auth){return service.atualizar(id,dto,auth);}
    @DeleteMapping("/{id}") @Operation(summary="Excluir categoria")
    public ResponseEntity<Void> excluir(@PathVariable Long id,Authentication auth){service.excluir(id,auth);return ResponseEntity.noContent().build();}
}
