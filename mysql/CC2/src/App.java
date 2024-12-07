
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class App {

	private static String URL = "jdbc:mysql://localhost:10002/coupedumonde"; //changer le nom de la bdd
	private static String login = "root";
	private static String password = "licinfo2020";
	private Connection connexion;
	private PreparedStatement stmt;
	private String query;
	private ArrayList<Object> params;
	private int nbChoix = 3;

	public App() {
		try {
			// Etablisement de la connexion avec la base
			connexion = DriverManager.getConnection(URL, login, password);
			menu();
		} catch (SQLException c) {
			System.out.println("Connexion echouee ou base de donnees inconnue : " + c);
		} catch (Exception d) {
			System.out.println("Problème sur connexion");
		}
	}

	public void menu() {

		int res = -1;
		while (res != 0) {
			// on propose à l'utilisateur de choisir entre plusieurs options
			Scanner scan = new Scanner(System.in);
			do {
				System.out.println("Menu\n0=Fin\n1=Lister toutes les equipes d'une poule\n2=Afficher la liste des stades (leur nom) avec leur nombre de matchs\n3=Ajouter un stade dans la base de données");
				res = scan.nextInt();
				if (res<0 || res > nbChoix) {
					System.out.println("mauvais choix! Recommencez.");
				}
			} while (res<0 || res > nbChoix);
			switch (res) {

			// lister toutes les équipes d'une poule
			case 1:
				System.out.println("Choississez le numero de la poule : ");
				try {

					// on lance la requête
					query = "SELECT nomEquipe FROM Equipe WHERE poule = ?";
                    params = new ArrayList<>();
                    params.add(scan.next());
                    int nbResultats = selectRequestWithDisplay(query, params);
				
					if (nbResultats == 0) {
                        System.out.println("La poule est vide ou le nom est incorrect !");
						System.out.println("Voici les noms de poule disponible : ");
						query = "SELECT DISTINCT poule FROM equipe";
						selectRequestWithDisplay(query, new ArrayList<>());
                    }

				} catch (SQLException c) {
					System.out.println("Connexion echouee ou base de donnees inconnue : " + c);
				}
				break;
				
			// afficher la liste des stades (leur nom) avec leur nombre de matchs
			case 2:
				try {
					query = "SELECT nomStade, count(leStade) FROM Stade, Matchs WHERE noStade=leStade GROUP BY nomStade ORDER BY count(leStade) ASC ";
					params = new ArrayList<>();
					selectRequestWithDisplay(query, params);
				} catch (SQLException c) {
					System.out.println("Problème lors de afficher la liste des stades (leur nom) avec leur nombre de matchs: " + c);
				}
				break;
				
			// ajouter un stade dans la base de données
			case 3:
				try {
					// on demande le nom de la ville 
					System.out.println("Entrez le nom d'une ville : ");
					Scanner s = new Scanner(System.in);
					String ville = s.nextLine();

					System.out.println("Entrez le nom du stade : ");
					String stade = s.nextLine();
					
					// on récupère les couples nomStades, ville existants 
					query = "SELECT nomStade, ville FROM Stade";
					ResultSet result = selectRequest(query, new ArrayList<>());

					boolean exist = false;
					int nbProchainStade = 1;
					while (result.next()) {
						if (ville.toLowerCase().equals(result.getString(2).toLowerCase()) && stade.toLowerCase().equals((result.getString(1).toLowerCase()))) {
							System.out.println("Ce stade a déjà été enregisté");	
							exist = true;
						}
						nbProchainStade += 1;
					}
					if (!exist) {
						query = "INSERT INTO Stade VALUES(?,?,?)";
						params = new ArrayList<>();
						params.add(nbProchainStade);
						params.add(stade);
						params.add(ville);
						otherRequest(query, params);

						System.out.println("Nouveau stade ajouté avec succès !");
					}
				} catch (SQLException c) {
					System.out.println("Problème lors de ajouter un stade dans la base de données: " + c);
				}
				break;
			}
		}

		// fermeture de la connexion
		try {
			connexion.close();
			System.out.println("Programme terminé");
		} catch (SQLException c) {
			System.out.println("Problème de fermeture de connexion: " + c);
		}

	}


		/**
	 * Exécute une requete de type select et renvoie le resultSet
	 * @param query
	 * @param params
	 * @return
	 * @throws SQLException
	 */
    public ResultSet selectRequest(String query, List<Object> params) throws SQLException{
        // Preparation de la requete
		stmt = connexion.prepareStatement(query);

        // Ajout des paramètre
        int index = 1;
        for (Object param : params) {
            if (param instanceof String) {
                stmt.setString(index, (String) param);
            }
            else if (param instanceof Integer) {
                stmt.setInt(index, (Integer) param);
            }
            index += 1;
        }
		// on renvoie le résultat
		return stmt.executeQuery();
    }



	/**
	 * Exécute une requete de type select, affiche les réponses et renvoie le nombre de ligne reçu 
	 * @param query
	 * @param params
	 * @return
	 * @throws SQLException
	 */
    public int selectRequestWithDisplay(String query, List<Object> params) throws SQLException{
        // Preparation de la requete
		stmt = connexion.prepareStatement(query);

        // Ajout des paramètre
        int index = 1;
        for (Object param : params) {
            if (param instanceof String) {
                stmt.setString(index, (String) param);
            }
            else if (param instanceof Integer) {
                stmt.setInt(index, (Integer) param);
            }
            index += 1;
        }
		// on récupère le résultat
		ResultSet resultat = stmt.executeQuery();

		// on parcourt le résultat
		int nbResultats = 0;
		while (resultat.next()) {
			StringBuilder row = new StringBuilder();
	
			// Parcours des colonnes
			for (int i = 1; i <= resultat.getMetaData().getColumnCount(); i++) {
				Object value = resultat.getObject(i);
				row.append(value).append(" "); 
			}
	
			System.out.println(row.toString().trim()); 
			nbResultats++;
		}

        // Renvoie le nombre de résultat
        return nbResultats;
    }


	/**
	 * Exécute une requete de type insert, update ou delete et renvoie le nombre de ligne modifiée
	 * @param query
	 * @param params
	 * @return
	 * @throws SQLException
	 */
    public int otherRequest(String query, List<Object> params) throws SQLException{
        // Preparation de la requete
		stmt = connexion.prepareStatement(query);

        // Ajout des paramètre
        int index = 1;
        for (Object param : params) {
            if (param instanceof String) {
                stmt.setString(index, (String) param);
            }
            else if (param instanceof Integer) {
                stmt.setInt(index, (Integer) param);
            }
            index += 1;
        }
		// on récupère le résultat
		int nbResultats = stmt.executeUpdate();
		
        // Renvoie le nombre de résultat
        return nbResultats;
    }


	public static void main(String[] args) {
		new App();
	}
}
