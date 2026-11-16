package br.com.fintrack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FinTrackSecurityIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    private String registerAndGetToken(String prefix) throws Exception {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        String body = """
            {"nome":"Usuário Teste","email":"%s","senha":"Senha123"}
            """.formatted(email);
        String response = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }

    @Test
    void endpointsFinanceirosExigemJwt() throws Exception {
        mvc.perform(get("/api/v1/categorias"))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/transacoes")
                .header("Authorization", "Bearer token-invalido"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioSoPodeExcluirSuaPropriaCategoria() throws Exception {
        String tokenA = registerAndGetToken("usuario-a");
        String tokenB = registerAndGetToken("usuario-b");

        String category = """
            {"nome":"Receita principal","descricao":"Teste de propriedade","tipo":"RECEITA"}
            """;
        String response = mvc.perform(post("/api/v1/categorias")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON).content(category))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long categoryId = objectMapper.readTree(response).get("id").asLong();

        mvc.perform(get("/api/v1/categorias")
                .header("Authorization", "Bearer " + tokenB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());

        mvc.perform(delete("/api/v1/categorias/" + categoryId)
                .header("Authorization", "Bearer " + tokenB))
            .andExpect(status().isForbidden());

        mvc.perform(delete("/api/v1/categorias/" + categoryId)
                .header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isNoContent());
    }

    @Test
    void recursoInexistenteRetorna404ERecursoDeOutroUsuarioRetorna403() throws Exception {
        String tokenA = registerAndGetToken("proprietario");
        String tokenB = registerAndGetToken("nao-proprietario");
        String category = """
            {"nome":"Categoria privada","descricao":"Teste","tipo":"RECEITA"}
            """;
        String response = mvc.perform(post("/api/v1/categorias")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON).content(category))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long categoryId = objectMapper.readTree(response).get("id").asLong();

        mvc.perform(delete("/api/v1/categorias/99999999")
                .header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isNotFound());

        mvc.perform(delete("/api/v1/categorias/" + categoryId)
                .header("Authorization", "Bearer " + tokenB))
            .andExpect(status().isForbidden());
    }

    @Test
    void modelMapperConverteTransacaoECalculaSaldoDoUsuario() throws Exception {
        String token = registerAndGetToken("mapeamento");
        String categoria = """
            {"nome":"Salário","descricao":"Renda mensal","tipo":"RECEITA"}
            """;
        String categoriaResponse = mvc.perform(post("/api/v1/categorias")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(categoria))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long categoriaId = objectMapper.readTree(categoriaResponse).get("id").asLong();

        String transacao = """
            {"descricao":"Salário de teste","valor":2500.00,"data":"2026-11-10",
             "tipo":"RECEITA","categoriaId":%d}
            """.formatted(categoriaId);
        mvc.perform(post("/api/v1/transacoes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(transacao))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.descricao").value("Salário de teste"))
            .andExpect(jsonPath("$.categoriaNome").value("Salário"));

        mvc.perform(get("/api/v1/transacoes/relatorios/saldo")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalReceitas").value(2500.0))
            .andExpect(jsonPath("$.totalDespesas").value(0.0))
            .andExpect(jsonPath("$.saldoAtual").value(2500.0));
    }

    @Test
    void cadastroDuplicadoRetornaConflict() throws Exception {
        String email = "duplicado-" + UUID.randomUUID() + "@example.com";
        String body = """
            {"nome":"Usuário Teste","email":"%s","senha":"Senha123"}
            """.formatted(email);

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated());

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isConflict());
    }
}
