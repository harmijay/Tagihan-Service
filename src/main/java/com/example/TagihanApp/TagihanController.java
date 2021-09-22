package com.example.TagihanApp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@Controller // This means that this class is a Controller
@RequestMapping(path="/tagihan") // This means URL's start with /demo (after Application path)
public class TagihanController {
    @Autowired // This means to get the bean called userRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private TagihanRepository tagihanRepository;

    @GetMapping("")
    public @ResponseBody Tagihan getTagihan(@RequestParam(value = "idTagihan") Long idTagihan) {
        return tagihanRepository.findById(idTagihan).get();
    }

    @GetMapping(path="/all")
    public @ResponseBody Iterable<Tagihan> getAllTagihan() {
        // This returns a JSON or XML with the users
        return tagihanRepository.findAll();
    }

    @PatchMapping(path="")
    public @ResponseBody String editTagihan (@RequestParam Long idTagihan,
        @RequestParam(required = false) Optional<Long> idPenagih,
        @RequestParam(required = false) Optional<Long> idYangDitagih,
        @RequestParam(required = false) Optional<BigDecimal> nominalTagihan) {

        Optional<Tagihan> on = tagihanRepository.findById(idTagihan);
        if (on.isEmpty()) { return "Failed to Find Tagihan by Id"; }
        Tagihan n = on.get();
        if (idPenagih.isPresent()) { n.setIdPenagih(idPenagih.get()); }
        if (idYangDitagih.isPresent()) { n.setIdYangDitagih(idYangDitagih.get()); }
        if (nominalTagihan.isPresent()) { n.setNominalTagihan(nominalTagihan.get()); }
        tagihanRepository.save(n);
        return "Tagihan Modified Succesfully";
    }

    @PostMapping(path="") // Map ONLY POST Requests
    public @ResponseBody String addNewTagihan (@RequestParam Long idPenagih
            , @RequestParam Long idYangDitagih, @RequestParam BigDecimal nominalTagihan) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request

        Tagihan n = new Tagihan();
        n.setIdPenagih(idPenagih);
        n.setIdYangDitagih(idYangDitagih);
        n.setNominalTagihan(nominalTagihan);
        tagihanRepository.save(n);
        return "Saved";
    }

    @PutMapping(path="")
    public @ResponseBody String replaceTagihan (@RequestParam Long idTagihan, @RequestParam Long idPenagih
            , @RequestParam Long idYangDitagih, @RequestParam BigDecimal nominalTagihan) {

        Optional<Tagihan> on = tagihanRepository.findById(idTagihan);
        if (on.isEmpty()) { return "Failed to Find Tagihan by Id"; }
        Tagihan n = on.get();
        n.setIdPenagih(idPenagih);
        n.setIdYangDitagih(idYangDitagih);
        n.setNominalTagihan(nominalTagihan);
        tagihanRepository.save(n);
        return "Tagihan Replaced Succesfully";
    }

    @DeleteMapping(path="")
    public @ResponseBody String deleteTagihan(@RequestParam Long idTagihan) {
        // This returns a JSON or XML with the users
        if(tagihanRepository.existsById(idTagihan)) {
            tagihanRepository.deleteById(idTagihan);
            return "Tagihan Deleted Succesfully";
        } else {
            return "Failed to Find Tagihan by Id";
        }
    }

//    @DeleteMapping(path="/all")
//    public @ResponseBody String deleteAllTagihan(@RequestParam Long idTagihan) {
//        // This returns a JSON or XML with the users
//        tagihanRepository.deleteAll();
//        return "All Tagihan Deleted Succesfully";
//    }
}