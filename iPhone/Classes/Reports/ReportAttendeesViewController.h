//
//  ReportAttendeesViewController.h
//  ConcurMobile
//
//  Created by yiwen on 5/26/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AddressBook/AddressBook.h>
#import <AddressBookUI/AddressBookUI.h>

#import "MobileViewController.h"
#import "AttendeeSearchDelegate.h"
#import "AttendeeActionDelegate.h"
#import "ReportAttendeeDelegate.h"
#import "EntryData.h"
#import "TextEditDelegate.h"

@class AttendeeActionManager;
@class AttendeeData;

@interface ReportAttendeesViewController : MobileViewController <  
                UITableViewDelegate
                , UITableViewDataSource
                , TextEditDelegate
                , AttendeeActionDelegate>
{
	id<ReportAttendeeDelegate>          __weak _delegate;
    
	UITableView							*tableView;
	
	AttendeeActionManager				*attendeeActionManager;
	NSMutableArray						*attendees;

	NSDecimalNumber						*transactionAmount;
	NSMutableArray                      *atnColumns;  // FormField definition for columns
    
	// Bump help dialog
	UILabel					*lblBumpHelpTitle;
	UILabel					*lblBumpHelpText1;
	UILabel					*lblBumpHelpText2;
	UIButton				*btnBumpShare;
	UIButton				*btnBumpCancel;
	UIImageView				*ivBumpBackground;
	UIView					*viewBumpHelp;
    
    BOOL                                isDirty;
    BOOL                                canEdit;
    
    // Info needed for rpeKey/ExpKey/polKey filtering
    NSArray                             *excludedAtnTypeKeys;
    EntryData                           *entry;
    NSString                            *expKey;
    NSString                            *polKey;
}

@property (strong, nonatomic) IBOutlet UILabel					*lblBumpHelpTitle;
@property (strong, nonatomic) IBOutlet UILabel					*lblBumpHelpText1;
@property (strong, nonatomic) IBOutlet UILabel					*lblBumpHelpText2;
@property (strong, nonatomic) IBOutlet UIButton					*btnBumpShare;
@property (strong, nonatomic) IBOutlet UIButton					*btnBumpCancel;
@property (strong, nonatomic) IBOutlet UIImageView				*ivBumpBackground;
@property (strong, nonatomic) IBOutlet UIView					*viewBumpHelp;

@property (nonatomic, weak) id<ReportAttendeeDelegate>	delegate;
@property BOOL isDirty;
@property BOOL canEdit;
@property (nonatomic, strong) IBOutlet UITableView					*tableView;

@property (nonatomic, strong) AttendeeActionManager					*attendeeActionManager;
@property (nonatomic, strong) NSMutableArray						*attendees;
@property (nonatomic, strong) NSString								*crnCode;
@property (nonatomic, strong) NSDecimalNumber						*transactionAmount;
@property (nonatomic, strong) NSArray                               *excludedAtnTypeKeys;
@property (nonatomic, strong) NSString								*expKey;
@property (nonatomic, strong) NSString								*polKey;
@property (nonatomic, strong) EntryData								*entry;

@property (nonatomic, strong) NSMutableArray                        *atnColumns;

-(void) configureAttendees:(NSMutableArray*)attendeesArray columns:(NSMutableArray*)atnColumns crnCode:(NSString*)crnCode transactionAmount:(NSDecimalNumber*)amount;

-(BOOL) isAttendeeEditable:(AttendeeData*)attendee;
-(BOOL) allowNoShow;
-(BOOL) canViewAttendee:(AttendeeData*) attendee;

// Editing Delegate methods
-(void) textUpdated:(NSObject*) context withValue:(NSString*) value;
// AttendeeActionDelegate methods
-(NSString*) getAttendeeCrnCode;
@end
