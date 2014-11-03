//
//  AttendeeEntryEditViewController.h
//  ConcurMobile
//
//  Created by yiwen on 10/6/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FormViewControllerBase.h"
#import "AttendeeData.h"
#import "AttendeeEditorDelegate.h"

@class MobileAlertView;

@interface AttendeeEntryEditViewController : FormViewControllerBase <AttendeeEditorDelegate>
{
	id<AttendeeEditorDelegate>			__weak _editorDelegate;
	NSDictionary						*dictInitialValuesForNewAttendee;
	AttendeeData						*attendee;
	NSArray								*attendeeTypes;
	NSArray								*attendeeTypeNames;
	BOOL								createdAttendee;
	BOOL								loadingAttendeeTypes;
	BOOL								loadingAttendeeForm;
	BOOL								savingAttendeeData;
	NSMutableArray						*errorAlerts;
    NSArray                             *excludedAtnTypeKeys;
    BOOL                                bAllowEditAtnAmt, bAllowEditAtnCount;
    NSString                            *crnCode;
    BOOL                                isAtnDirty;
}

@property (weak, nonatomic) id<AttendeeEditorDelegate>		editorDelegate;
@property (nonatomic, strong) NSDictionary						*dictInitialValuesForNewAttendee;
@property (nonatomic, strong) AttendeeData						*attendee;
@property (nonatomic, strong) NSArray							*attendeeTypes;
@property (nonatomic, strong) NSArray							*attendeeTypeNames;
@property (nonatomic, assign) BOOL								createdAttendee;
@property (nonatomic, assign) BOOL								loadingAttendeeTypes;
@property (nonatomic, assign) BOOL								loadingAttendeeForm;
@property (nonatomic, assign) BOOL								savingAttendeeData;
@property (nonatomic, strong) NSMutableArray					*errorAlerts;
@property (nonatomic, strong) NSArray                           *excludedAtnTypeKeys;
@property (nonatomic, strong) NSString                          *crnCode;

@property BOOL                                                  bAllowEditAtnAmt;
@property BOOL                                                  bAllowEditAtnCount;
@property BOOL                                                  isAtnDirty;
@property BOOL                                                  isDisplayZero;

-(void) configureWaitView;
-(void) createAttendeeWithInitialValues:(NSDictionary*)valuesDict;
-(void) editAttendee:(AttendeeData*)attendeeData;
-(void) loadAttendeeTypes;
-(void) loadAttendeeFormForAttendeeTypeKey:(NSString*)atnTypeKey attendeeKey:(NSString*)attnKey;
-(void) showErrorMessage:(NSString*)message;
-(void) alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex;
-(void) close;

#pragma mark AttendeeEditorDelegate
// To support callback from AttendeeDuplicateVC
-(void)editedAttendee:(AttendeeData*)attendee createdByEditor:(BOOL)created;
-(BOOL)canEdit;

@end
