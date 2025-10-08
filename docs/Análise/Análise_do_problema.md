# PSOFT- Análise do problema

Queremos criar um sistema que permite o registo de pacientes e de psiquiatras, o agendamento de compromissos e o registo dos detalhes dos compromissos.
Um hospital necessita de um sistema backend para gerir o histórico dos seus pacientes. O sistema necessita de controlar milhares de perfis de pacientes e de perfis de psiquiatras enquanto permite o agendamento de compromisso e a captura de detalhes de cada compromisso(por exemplo, diagnósticos, tratamentos e prescrições). A camada de serviço tem de expor as suas funcionalidades por via de APIs RESTful para que qualquer frontend(web ou mobile) consiga interegir com o mesmo. Ainda, o sistema precisa de suportar a exploração de data e reportar para motivos administrativos.

É necessário localizar os 5 melhores psiquiatras por número de compromissos, a média por mês de compromissos por psiquiatra e a média de tempo de consulta por psiquiatra e por paciente.

Limites operacionais: Compromissos só podem ser agendados caso o paciente esteja registado e caso o psiquiatra esta disponível. Os pacientes só podem ver os seus próprios registos enquanto a administração conseguem ver qualquer informação.

# Fase 1

## WP 0A

### UC0:Necessidade de inicializar o sistema para os admins, inicializar usando credenciais(username e password) para os admins do sistema. Os dados de inicialização devem conter departamentos e especialização de cada psiquiatra.

## WP 1A Médicos

### US1:Um admin quer registar um médico com detalhes como nome completo, especialidade(única), contacto(email e telefone obrigatórios pelo menos 1 de cada, informação(morada obrigatória pelo menos uma), horas de trabalho(
### hora de inicio e hora de fim igual para toda a semana), departamento(único e caracterizado por sua sigla DC(Departamento de Cardiologia) e uma descrição opcional do psiquiatra.

### US2:Como admin ou paciente, quero ver detalhes de um médico dado o seu ID.

### US3:Como paciente quer procurar médicos pelo nome ou pela especialidade. O admin deve atribuir um email e uma password ao médico quando o colocar no sistema.

## WP 2A Pacientes

### US4:Como um utilizador anónimo, quero registar me como paciente dando o nome completo, email, data de nascimento, telefone, informação de seguro(refere-se ao número de apólice e ao nome da companhia de seguro e pode ou não possuir seguro) e consentimento de dados(ou seja tem de se registar que o paciente consente e a data do consentimento). Um ID único de paciente será automaticamente criado.
### US5:Como um administrador quero ver os detalhes de um paciente dado o seu ID.
### US6:Como um admin quero procurar os pacientes pelo seu nome. Um paciente PODE ser tratado por mais de um médico.

## WP3 Compromissos

### US7:Como um admin quero agendar um compromisso com um médico escolhido, escolhendo a data, a hora e o tipo de consulta. Assume-se que o psiquiatra está disponível.
### US8:Como paciente quero ver todos os meus compromissos. 
### US9:Como paciente ou admin quero atualizar ou cancelar um compromisso. 
### US10:Como paciente quero ver todos os detalhes de um compromisso dado o número de um compromisso. Quando uma marcação é cancelada, a mesma NÃO é apagada, em vez disso, mantém se no registo para análise estatística. As marcações devem ser organizadas em slots de tempo de 20 minutos e o horário do hospital é:
### 9:00 - 13:00
### 14:00 - 20:00
### segunda a sexta


### 9:00 - 13:00
### sábado

## WP4 Registo de compromissos

### US11:Como médico quero registar os detalhes de um compromisso(como diagnóstico, tratamento, recomendações, prescrições) imediatamente após consulta. 
### US12:Como médico quero ver o registo dos compromissos de um paciente.
### US13:Como paciente quero ver os meus registos de compromisso.
### US14:Como admin ou paciente, quero ver os registos de compromissos usando o seu número de registo.

## Requerimentos não funcionais

Todos os pedidos de autenticação devem usar JWT, a especificação da OPENAPI, amostras de pedidos e respostas como por exemplo coleção de POSTMAN. Testes automatizados por exemplo coleção de POSTMAN.
Providenciar links na representação de recursos.(professor diz que podemos ignorar este requerimento no primeiro envio).
No login de utilizador é necessário que o username seja o email e que a palavra passe tenha no mínimo 10 caracteres, uma maiúscula, um digito e um caracter especial.
Os pacientes só têm acesso ao nome e especialidade do médico(restrição de acesso a certos atributos do psiquiatra).
Na pesquisa por médicos é possível o paciente pesquisar por apenas uma parte do nome para encontrar todos os psiquiatras com esse nome. Vice-versa para a pesquisa de pacientes.
O paciente vai poder acessar ao seu histórico de consultas
Os atributos morada e nmr de telefone não são únicos.
O paciente quando vê o seu histórico de consultas deve ver primeiro as mais recentes até as mais antigas.

