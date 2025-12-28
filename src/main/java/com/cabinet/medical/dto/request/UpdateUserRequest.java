package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**

* DTO UpdateUserRequest
* Mise à jour profil utilisateur
* Tous les champs optionnels (seulement les champs modifiés sont envoyés)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {

    /**
  * Nouveau prénom (optionnel)
  * Si null, firstName non modifié
     */
    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    private String firstName;

    /**
  * Nouveau nom (optionnel)
     */
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String lastName;

    /**
  * Nouveau téléphone (optionnel)
  * null = supprimer téléphone
  * "" = supprimer téléphone
  * "0612345678" = nouveau numéro
     */
    @Pattern(regexp = "^(0[1-9]\\d{8})?$",
             message = "Format téléphone invalide (10 chiffres commençant par 0)")
    private String phone;
}
