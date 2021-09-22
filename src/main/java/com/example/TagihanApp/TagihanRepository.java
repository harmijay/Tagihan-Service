package com.example.TagihanApp;

import org.springframework.data.repository.CrudRepository;

import com.example.TagihanApp.Tagihan;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface TagihanRepository extends CrudRepository<Tagihan, Long> {

}