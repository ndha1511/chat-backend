package com.project.chatbackend.controllers;

import com.project.chatbackend.requests.AcceptAddFriendRequest;
import com.project.chatbackend.requests.RejectAddFriendRequest;
import com.project.chatbackend.requests.SendAddFriendRequest;
import com.project.chatbackend.requests.UnfriendRequest;
import com.project.chatbackend.services.AddFriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {
    @Autowired
    private final AddFriendService addFriendService;

    //send friend request
    @PostMapping("/add")
    public ResponseEntity<?> sendFriendRequest(@RequestBody  SendAddFriendRequest sendAddFriendRequest){
       try{
              addFriendService.sendAddFriendRequest(sendAddFriendRequest);
              return ResponseEntity.ok("Request sent");
       }catch (Exception e){
              return ResponseEntity.badRequest().body(e.getMessage());
       }
    }

    //accept friend request
    @PostMapping("/accept")
    public ResponseEntity<?> acceptFriendRequest(@RequestBody AcceptAddFriendRequest sendAddFriendRequest){
       try{
              addFriendService.acceptAddFriendRequest(sendAddFriendRequest);
              return ResponseEntity.ok("Request accepted");
       }catch (Exception e){
              return ResponseEntity.badRequest().body(e.getMessage());
       }
    }

    //reject friend request
    @PostMapping("/reject")
    public ResponseEntity<?> rejectFriendRequest(@RequestBody RejectAddFriendRequest sendAddFriendRequest){
       try{
              addFriendService.rejectAddFriendRequest(sendAddFriendRequest);
              return ResponseEntity.ok("Request rejected");
       }catch (Exception e){
              return ResponseEntity.badRequest().body(e.getMessage());
       }
    }

    @PostMapping("/unfriend")
    public ResponseEntity<?> unfriend(@RequestBody UnfriendRequest unfriendRequest){
       try{
              addFriendService.unfriend(unfriendRequest);
              return ResponseEntity.ok("Unfriended");
       }catch (Exception e){
              return ResponseEntity.badRequest().body(e.getMessage());
       }
    }

    @PostMapping("/get-friend-request")
    public ResponseEntity<?> getFriendRequest(@RequestBody String email){
       try{
              return ResponseEntity.ok(addFriendService.getRequestAddFriendByReceiverId(email));
       }catch (Exception e){
              return ResponseEntity.badRequest().body(e.getMessage());
       }
    }

    @PostMapping("/get-friend-request-by-sender-id")
    public ResponseEntity<?> getFriendRequestBySenderId(@RequestBody String email){
       try{
              return ResponseEntity.ok(addFriendService.getRequestAddFriendBySenderId(email));
       }catch (Exception e){
              return ResponseEntity.badRequest().body(e.getMessage());
       }
    }



}
