package br.com.fintrack.controller;
import br.com.fintrack.dto.*;
import br.com.fintrack.service.TransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transacoes")
@Tag(name="Transações")
@SecurityRequirement(name="bearerAuth")
@PreAuthorize("hasRole('USER')")
public class TransacaoController {
    private final TransacaoService service;
    public TransacaoController(TransacaoService service){this.service=service;}
    @GetMapping @Operation(summary="Listar transações; filtros opcionais por período e categoria")
    public List<TransacaoDTO> listar(Authentication auth,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate inicio,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fim,
        @RequestParam(required=false) Long categoriaId){return service.listar(auth,inicio,fim,categoriaId);}
    @PostMapping @Operation(summary="Cadastrar receita ou despesa")
    public ResponseEntity<TransacaoDTO> criar(@Valid @RequestBody TransacaoDTO dto,Authentication auth){return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto,auth));}
    @PutMapping("/{id}") @Operation(summary="Atualizar transação")
    public TransacaoDTO atualizar(@PathVariable Long id,@Valid @RequestBody TransacaoDTO dto,Authentication auth){return service.atualizar(id,dto,auth);}
    @DeleteMapping("/{id}") @Operation(summary="Excluir transação")
    public ResponseEntity<Void> excluir(@PathVariable Long id,Authentication auth){service.excluir(id,auth);return ResponseEntity.noContent().build();}
    @GetMapping("/relatorios/saldo") @Operation(summary="Consultar total de receitas, despesas e saldo")
    public SaldoDTO saldo(Authentication auth){return service.saldo(auth);}
}
