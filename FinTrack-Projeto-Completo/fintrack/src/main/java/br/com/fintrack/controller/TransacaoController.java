package br.com.fintrack.controller;

import br.com.fintrack.dto.*;
import br.com.fintrack.service.TransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name="Transações", description="CRUD de receitas e despesas do usuário autenticado")
@SecurityRequirement(name="bearerAuth")
@PreAuthorize("hasRole('USER')")
@ApiResponses({
    @ApiResponse(responseCode="401", description="Token ausente, inválido ou expirado"),
    @ApiResponse(responseCode="403", description="Usuário autenticado não é o proprietário do recurso")
})
public class TransacaoController {
    private final TransacaoService service;
    public TransacaoController(TransacaoService service){this.service=service;}

    @GetMapping
    @Operation(summary="Listar transações; filtros opcionais por período e categoria")
    @ApiResponses({
        @ApiResponse(responseCode="200", description="Lista de transações"),
        @ApiResponse(responseCode="400", description="Intervalo de datas inválido")
    })
    public List<TransacaoDTO> listar(Authentication auth,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate inicio,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fim,
        @RequestParam(required=false) Long categoriaId){
        return service.listar(auth,inicio,fim,categoriaId);
    }

    @PostMapping
    @Operation(summary="Cadastrar receita ou despesa")
    @ApiResponses({
        @ApiResponse(responseCode="201", description="Transação criada"),
        @ApiResponse(responseCode="400", description="Dados inválidos ou tipo incompatível com a categoria"),
        @ApiResponse(responseCode="404", description="Categoria não encontrada para o usuário"),
        @ApiResponse(responseCode="409", description="Saldo insuficiente")
    })
    public ResponseEntity<TransacaoDTO> criar(@Valid @RequestBody TransacaoDTO dto,Authentication auth){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto,auth));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') and @transacaoService.isOwner(authentication, #p0)")
    @Operation(summary="Atualizar transação pertencente ao usuário")
    @ApiResponses({
        @ApiResponse(responseCode="200", description="Transação atualizada"),
        @ApiResponse(responseCode="400", description="Dados inválidos"),
        @ApiResponse(responseCode="403", description="Transação pertence a outro usuário"),
        @ApiResponse(responseCode="404", description="Transação ou categoria não encontrada"),
        @ApiResponse(responseCode="409", description="Saldo insuficiente")
    })
    public TransacaoDTO atualizar(@PathVariable Long id,@Valid @RequestBody TransacaoDTO dto,Authentication auth){
        return service.atualizar(id,dto,auth);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') and @transacaoService.isOwner(authentication, #p0)")
    @Operation(summary="Excluir transação pertencente ao usuário")
    @ApiResponses({
        @ApiResponse(responseCode="204", description="Transação excluída"),
        @ApiResponse(responseCode="403", description="Transação pertence a outro usuário"),
        @ApiResponse(responseCode="404", description="Transação não encontrada")
    })
    public ResponseEntity<Void> excluir(@PathVariable Long id,Authentication auth){
        service.excluir(id,auth);return ResponseEntity.noContent().build();
    }

    @GetMapping("/relatorios/saldo")
    @Operation(summary="Consultar total de receitas, despesas e saldo")
    @ApiResponse(responseCode="200", description="Resumo financeiro do usuário autenticado")
    public SaldoDTO saldo(Authentication auth){return service.saldo(auth);}
}
