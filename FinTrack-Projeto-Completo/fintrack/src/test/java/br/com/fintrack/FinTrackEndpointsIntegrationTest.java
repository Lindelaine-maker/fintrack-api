package br.com.fintrack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FinTrackEndpointsIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    private String register(String prefix) throws Exception {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        String body = """
            {"nome":"Teste FinTrack","email":"%s","senha":"Senha123"}
            """.formatted(email);
        String response = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("token").asText();
    }

    private String json(Object value) throws Exception {
        return mapper.writeValueAsString(value);
    }

    @Test
    void loginEFluxoCompletoDeCategoriasTransacoesEFiltros() throws Exception {
        String email = "fluxo-" + UUID.randomUUID() + "@example.com";
        String registerBody = """
            {"nome":"Teste Fluxo","email":"%s","senha":"Senha123"}
            """.formatted(email);
        String registered = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(registerBody))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        String token = mapper.readTree(registered).get("token").asText();

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"%s","senha":"Senha123"}
                    """.formatted(email)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty());

        String auth = "Bearer " + token;
        String incomeCategoryBody = """
            {"nome":"Receitas %s","descricao":"Entradas","tipo":"RECEITA"}
            """.formatted(UUID.randomUUID());
        String expenseCategoryBody = """
            {"nome":"Despesas %s","descricao":"Saídas","tipo":"DESPESA"}
            """.formatted(UUID.randomUUID());

        String incomeCategoryResponse = mvc.perform(post("/api/v1/categorias")
                .header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content(incomeCategoryBody))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long incomeCategoryId = mapper.readTree(incomeCategoryResponse).get("id").asLong();

        String expenseCategoryResponse = mvc.perform(post("/api/v1/categorias")
                .header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content(expenseCategoryBody))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long expenseCategoryId = mapper.readTree(expenseCategoryResponse).get("id").asLong();

        mvc.perform(get("/api/v1/categorias").header("Authorization", auth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == " + incomeCategoryId + ")]").exists())
            .andExpect(jsonPath("$[?(@.id == " + expenseCategoryId + ")]").exists());

        mvc.perform(put("/api/v1/categorias/" + incomeCategoryId)
                .header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Receitas atualizadas %s","descricao":"Atualizada","tipo":"RECEITA"}
                    """.formatted(UUID.randomUUID())))
            .andExpect(status().isOk());

        String incomeResponse = mvc.perform(post("/api/v1/transacoes")
                .header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"descricao":"Receita de teste","valor":3500.00,"data":"2026-11-05","tipo":"RECEITA","categoriaId":%d}
                    """.formatted(incomeCategoryId)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long incomeId = mapper.readTree(incomeResponse).get("id").asLong();

        String expenseResponse = mvc.perform(post("/api/v1/transacoes")
                .header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"descricao":"Despesa de teste","valor":450.00,"data":"2026-11-06","tipo":"DESPESA","categoriaId":%d}
                    """.formatted(expenseCategoryId)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long expenseId = mapper.readTree(expenseResponse).get("id").asLong();

        mvc.perform(get("/api/v1/transacoes").header("Authorization", auth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == " + incomeId + ")]").exists())
            .andExpect(jsonPath("$[?(@.id == " + expenseId + ")]").exists());

        mvc.perform(get("/api/v1/transacoes")
                .header("Authorization", auth)
                .param("inicio", "2026-11-05").param("fim", "2026-11-05")
                .param("categoriaId", String.valueOf(incomeCategoryId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));

        mvc.perform(put("/api/v1/transacoes/" + expenseId)
                .header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"descricao":"Despesa atualizada","valor":500.00,"data":"2026-11-06","tipo":"DESPESA","categoriaId":%d}
                    """.formatted(expenseCategoryId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valor").value(500.00));

        mvc.perform(get("/api/v1/transacoes/relatorios/saldo")
                .header("Authorization", auth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalReceitas").value(3500.00))
            .andExpect(jsonPath("$.totalDespesas").value(500.00))
            .andExpect(jsonPath("$.saldoAtual").value(3000.00));

        mvc.perform(delete("/api/v1/transacoes/" + expenseId)
                .header("Authorization", auth))
            .andExpect(status().isNoContent());
        mvc.perform(delete("/api/v1/transacoes/" + incomeId)
                .header("Authorization", auth))
            .andExpect(status().isNoContent());

        mvc.perform(delete("/api/v1/categorias/" + expenseCategoryId)
                .header("Authorization", auth))
            .andExpect(status().isNoContent());
        mvc.perform(delete("/api/v1/categorias/" + incomeCategoryId)
                .header("Authorization", auth))
            .andExpect(status().isNoContent());
    }

    @Test
    void validacaoDeTransacaoInvalidaRetornaBadRequest() throws Exception {
        String token = register("validacao");
        mvc.perform(post("/api/v1/transacoes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"descricao":"Valor inválido","valor":0,"data":"2026-11-05","tipo":"RECEITA","categoriaId":1}
                    """))
            .andExpect(status().isBadRequest());
    }
}
