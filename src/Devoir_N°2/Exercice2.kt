import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class Produit(val id: Int, val nom: String, var quantite: Int) {
    fun afficherDetails() = println("$nom (ID: $id) → Quantité: $quantite")
}

class Stock {
    private val produits = mutableMapOf<Int, Produit>()
    private val _stockFlow = MutableSharedFlow<String>()
    val stockFlow: SharedFlow<String> = _stockFlow.asSharedFlow()

    suspend fun ajouterProduit(produit: Produit) {
        produits[produit.id] = produit
        _stockFlow.emit("Ajout: ${produit.nom} (x${produit.quantite})")
    }

    suspend fun ajouterQuantite(idProduit: Int, quantite: Int) {
        produits[idProduit]?.let { p ->
            p.quantite += quantite
            _stockFlow.emit("+$quantite → ${p.nom} (total: ${p.quantite})")
        }
    }

    suspend fun retirerQuantite(idProduit: Int, quantite: Int) {
        val p = produits[idProduit]
        if (p == null || p.quantite < quantite) {
            throw IllegalArgumentException("Stock insuffisant pour ${p?.nom ?: "ID:$idProduit"}")
        }
        p.quantite -= quantite
        _stockFlow.emit("$quantite → ${p.nom} (restant: ${p.quantite})")
    }

    fun afficherStock() {
        println("=== STOCK ACTUEL ===")
        produits.values.forEach { it.afficherDetails() }
    }
}


data class CommandeClient(val idCommande: Int, val produits: List<Pair<Int, Int>>) {
    fun afficherCommande() {
        println("Commande $idCommande :")
        produits.forEach { (id, qte) -> println("   Produit $id × $qte") }
    }
}

class GestionnaireCommandes(val stock: Stock) {
    suspend fun traiterCommande(commande: CommandeClient): Boolean = try {
        commande.produits.forEach { (id, qte) ->
            stock.retirerQuantite(id, qte)
        }
        println("Commande ${commande.idCommande} traitée")
        true
    } catch (e: Exception) {
        println("Échec commande ${commande.idCommande} : ${e.message}")
        false
    }

    suspend fun gererCommandes(commandes: List<CommandeClient>) = coroutineScope {
        commandes.forEach { cmd ->
            launch { traiterCommande(cmd) }
        }
    }
}


class Entrepot {
    private val stock = Stock()
    private val gestionnaire = GestionnaireCommandes(stock)

    val evenementsStock = stock.stockFlow

    suspend fun ajouterProduitAuStock(produit: Produit) {
        stock.ajouterProduit(produit)
    }

    suspend fun retirerProduitDuStock(id: Int, quantite: Int) {
        try {
            stock.retirerQuantite(id, quantite)
        } catch (e: Exception) {
            println("Impossible de retirer : ${e.message}")
        }
    }

    suspend fun gererInventaire() {
        println("Inventaire en cours...")
        delay(500)
        stock.afficherStock()
    }
}


suspend fun main() = coroutineScope {
    val entrepot = Entrepot()


    entrepot.ajouterProduitAuStock(Produit(1, "Lait", 50))
    entrepot.ajouterProduitAuStock(Produit(2, "Pain", 100))


    launch {
        entrepot.evenementsStock.collect { event ->
            println("$event")
        }
    }


    val commandes = listOf(
        CommandeClient(1, listOf(1 to 5, 2 to 10)),
        CommandeClient(2, listOf(1 to 60)) // Échouera
    )

    launch { entrepot.gererCommandes(commandes) }
    launch { entrepot.gererInventaire() }

    delay(2000)
}