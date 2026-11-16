package br.com.fintrack.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="categorias", uniqueConstraints=@UniqueConstraint(columnNames={"nome","usuario_id"}))
public class Categoria {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, length=80) private String nome;
    @Column(length=200) private String descricao;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=10) private TipoTransacao tipo;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="usuario_id") private Usuario usuario;
    @OneToMany(mappedBy="categoria") private List<Transacao> transacoes = new ArrayList<>();
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getNome(){return nome;} public void setNome(String nome){this.nome=nome;}
    public String getDescricao(){return descricao;} public void setDescricao(String descricao){this.descricao=descricao;}
    public TipoTransacao getTipo(){return tipo;} public void setTipo(TipoTransacao tipo){this.tipo=tipo;}
    public Usuario getUsuario(){return usuario;} public void setUsuario(Usuario usuario){this.usuario=usuario;}
}
