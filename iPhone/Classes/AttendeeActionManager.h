//
//  AttendeeActionManager.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 1/14/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AddressBook/AddressBook.h>
#import <AddressBookUI/AddressBookUI.h>
#import "MobileViewController.h"
#import "AttendeeSearchDelegate.h"
#import "AttendeeEditorDelegate.h"
#import "AttendeeActionDelegate.h"
#import "ExSystem.h"

// The same actions (create, edit) can be taken on an attendee
// from different view controllers:
//
//		OutOfPocketFormViewController for expense, and
//		ReportEntryViewController for report editing.
//
// This class handles attendee creation and editing on behalf
// of those view controllers.
//
@interface AttendeeActionManager : NSObject < UIActionSheetDelegate
											, ABPeoplePickerNavigationControllerDelegate
											, ABNewPersonViewControllerDelegate
											, AttendeeSearchDelegate
											, AttendeeEditorDelegate>
{
	id<AttendeeActionDelegate>	__weak _attendeeActionDelegate;
	MobileViewController		*__weak viewController;	// Set this pointer to nil when the view controller it points to is released.
	NSMutableArray				*attendees;
    BOOL                        canEdit;
    
    // Info needed for rpeKey/ExpKey/polKey filtering
    NSString                            *rpeKey;
    NSString                            *expKey;
    NSString                            *polKey;
    
    // Info needed to update attendee list
    NSArray                     *atnColumns;

}

// Assign is used for the attendeeActionDelegate to prevent circular retain references where
// this instance of AttendeeActionManager retains a view controller that itself retains this instance.
@property (nonatomic, weak) id<AttendeeActionDelegate>	attendeeActionDelegate;
@property (nonatomic, weak) MobileViewController			*viewController;
@property (nonatomic, strong) NSMutableArray				*attendees;
@property BOOL  canEdit;

@property (nonatomic, strong) NSString								*expKey;
@property (nonatomic, strong) NSString								*polKey;
@property (nonatomic, strong) NSString								*rpeKey;

@property (nonatomic, strong) NSArray                       *atnColumns;

-(id) initWithViewController:(MobileViewController*)mvc attendeeActionDelegate:(id<AttendeeActionDelegate>)actionDelegate canEdit:(BOOL)editFlag;
-(void) showAttendeeActionSheetForAttendees:(NSMutableArray*)attendeesArray;
-(void) showAttendeeActionSheetForAttendees:(NSMutableArray*)attendeesArray clicked:(id)sender;

-(void) showAttendeeEditor:(AttendeeData*)attendee parentView:(BOOL)isItemizedParentView;

-(void) showContactList;
-(void) showAddContact;
-(void) showAttendeeSearch;
-(void) showManualAttendee;

-(void)addAttendee:(NSString*) lastName firstName:(NSString*) firstName companyName:(NSString*) companyName;

@end
