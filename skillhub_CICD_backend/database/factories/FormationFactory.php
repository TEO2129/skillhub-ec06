<?php

namespace Database\Factories;

use App\Models\Formation;
use App\Models\User;
use Illuminate\Database\Eloquent\Factories\Factory;

class FormationFactory extends Factory
{
    protected $model = Formation::class;

    public function definition(): array
    {
        return [
            'titre' => $this->faker->sentence(3),
            'description' => $this->faker->paragraph(),
            'categorie' => $this->faker->randomElement(['informatique', 'design', 'marketing', 'data', 'autre']),
            'niveau' => $this->faker->randomElement(['debutant', 'intermediaire', 'avance']),
            'prix' => $this->faker->randomFloat(2, 0, 100),
            'duree_heures' => $this->faker->numberBetween(1, 40),
            'nombre_de_vues' => 0,
            'formateur_id' => User::factory(),
            'created_at' => now(),
            'updated_at' => now(),
        ];
    }
}
