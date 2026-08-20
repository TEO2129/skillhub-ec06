<?php

namespace Database\Factories;

use App\Models\Inscription;
use App\Models\User;
use App\Models\Formation;
use Illuminate\Database\Eloquent\Factories\Factory;

class InscriptionFactory extends Factory
{
    protected $model = Inscription::class;

    public function definition(): array
    {
        return [
            'user_id' => User::factory(),  // Changé de utilisateur_id à user_id
            'formation_id' => Formation::factory(),
            'progression' => $this->faker->numberBetween(0, 100),
            'last_activity_at' => $this->faker->optional()->dateTimeBetween('-60 days', 'now'),
            'created_at' => now(),
            'updated_at' => now(),
        ];
    }
}
