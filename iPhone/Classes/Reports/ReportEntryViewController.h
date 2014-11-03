//
//  ReportEntryViewController.h
//  ConcurMobile
//
//  Created by yiwen on 4/28/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ReportHeaderViewControllerBase.h"
#import "ExpenseTypeDelegate.h"
#import "ReportAttendeeDelegate.h"
#import "CarRatesData.h"
#import "ReceiptEditorDelegate.h"
#import "Receipt.h"

@interface ReportEntryViewController : ReportHeaderViewControllerBase
    <UIActionSheetDelegate,
    UINavigationControllerDelegate, 
    ExpenseTypeDelegate,
    ReceiptEditorDelegate,
    ReportAttendeeDelegate>
{
    NSMutableArray			*attendees; // Editable copy of entry.attendees
    
    CarRatesData            *carRates;
}

@property (strong, nonatomic) NSMutableArray			*attendees;
@property (strong, nonatomic) EntryData					*entry;
@property (strong, nonatomic) Receipt                   *receipt;

@property BOOL                                          isCarMileage;
@property BOOL isFromHome;
@property BOOL originalIsCCEntry;
@property (assign, nonatomic) BOOL                      entryUpdatedImageId;

// Init data
- (id)initWithCloseButton:(BOOL)withCloseButton;
- (void)setSeedData:(ReportData*)report entry:(EntryData*)thisEntry role:(NSString*) curRole;
- (void)loadEntry:(EntryData*) thisEntry withReport:(ReportData*) report;
- (void)refreshView;
- (void)fetchEntryDetail;
- (void)recalculateSections;
- (void)configureDrillData:(NSMutableDictionary*)newSectionDataMap sections:(NSMutableArray*)newSections;
- (NSDictionary*) getComments;

// Common API shared between entry and itemization
- (void)initFieldsWithEntry:(EntryData*) theEntry forSection:(NSString*) sectionName;
- (void)updateAmountFieldsCommon:(FormFieldData*) field;
- (void)recalculatePostedAmount;
- (void)respondToEntryForm:(Msg *)msg;
-(void)respondToEntrySave:(Msg *)msg;

- (void)showAttendeeEditor;
- (BOOL)hasAttendeesField;
- (FormFieldData*)lookupAttendeesField;
- (BOOL)allowEditAttendees;
- (void)recalculateAttendeeAmounts;

// Entry specific
- (void)refreshCarRates;
- (void)addCarMileageFields:(EntryData*) theEntry isPersonal:(BOOL)isPerCarExpType;
- (void)updateAmountFields:(FormFieldData*) field;
- (NSString*)getDistanceUnit;

// Action methods
- (void)goToItemization;
- (void)goToReportDetailScreen;

// ExpenseType delegate
- (void)saveSelectedExpenseType:(ExpenseTypeData*)et;

// Entry edit methods
- (NSString*)getCurrentExpType;
- (BOOL)isPersonalCarMileageExpType:(NSString*)expType;
- (BOOL)isCompanyCarMileageExpType:(NSString*)expType;
- (NSString*)lookupCurrencyCode;

// Merge existing changes to the set of fields passed in.  Return an array of fields for display.
//- (NSMutableArray*)mergeFields:(NSDictionary*) fields withKeys:(NSArray*) keys; // Form data
//-(void) mergeChanges:(ReportDetailDataBase*) fData; // Form data

// Save mthods
- (void)sendSaveReportMsg;

-(void)attendeesEdited:(NSMutableArray*)editedAttendees;
-(BOOL)isParentEntry;

// For subclass to customize details section
-(NSArray*) getFieldsInDetailsSection;

@end
