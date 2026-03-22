package com.example.BuildingManagement.room.Controller;

import com.example.BuildingManagement.room.Model.Room;
import com.example.BuildingManagement.room.Model.RoomRequest;
import com.example.BuildingManagement.room.Service.RoomService;
import com.example.BuildingManagement.user.Security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/properties/apartments/{apartmentId}/rooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LANDLORD')")
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<Room> createRoom(
            @PathVariable Long apartmentId,
            @Valid @RequestBody RoomRequest roomRequest,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(roomService.addRoom(apartmentId, roomRequest, principal.getUser()));
    }

    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms(
            @PathVariable Long apartmentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(roomService.getRoomsByApartment(apartmentId, principal.getUser()));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<Room> getRoomById(
            @PathVariable Long apartmentId,
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(roomService.getRoomById(apartmentId, roomId, principal.getUser()));
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<Room> updateRoom(
            @PathVariable Long apartmentId,
            @PathVariable Long roomId,
            @Valid @RequestBody RoomRequest roomRequest,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(roomService.updateRoom(apartmentId, roomId, roomRequest, principal.getUser()));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<String> deleteRoom(
            @PathVariable Long apartmentId,
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserPrincipal principal) {
        roomService.deleteRoom(apartmentId, roomId, principal.getUser());
        return ResponseEntity.ok("Room deleted successfully within the context of Apartment " + apartmentId);
    }
}