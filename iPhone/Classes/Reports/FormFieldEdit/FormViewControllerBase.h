//
//  FormViewControllerBase.h
//  ConcurMobile
//
//  Created by yiwen on 4/19/11.
//  This base class maintains the following
//  - list of fields (FormFieldData) to be edited
//  - isDirty flag
//  - Save button on the right on top bar.  Do not show it if nothing to edit.
//  - Invoke editors, according to field data type.
//  - Provide call backs after finishing editing of fields
//  
//  This class assumes that the fields will be displayed in tableList (UITableView), and the table might contain cells other than fields.
//  This class supports UI elements other than the tableList.
//
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "FieldEditDelegate.h"
#import "EditFormHelper.h"
#import "TextEditDelegate.h"
#import "ListFieldEditVC.h"
#import "DateEditDelegate.h"
#import "BoolEditDelegate.h"
#import "DateTimePopoverVC.h"
#import "GoogleRouteFinderHandler.h"

@interface FormViewControllerBase : MobileViewController<
				UITableViewDelegate
				,UITableViewDataSource
                ,BoolEditDelegate
                ,EditFormDataSource
                ,TextEditDelegate
                ,DateEditDelegate
                ,DateTimePopoverDelegate
				,FieldEditDelegate
                ,GoogleHandlerDelegate>
{
    UITableView				*tableList;
    
    GoogleRouteFinderHandler *googleHandler;
    
	NSMutableArray          *sections;
    NSMutableDictionary     *sectionDataMap; // Non-field data map for sections
    
	NSMutableDictionary     *sectionFieldsMap;
	NSMutableArray			*allFields;	// All known fields, parent fields


	// Fields used to switch forms, this edit form knows how to switch forms
	NSString				*formKey;

	BOOL					isDirty;
    
    BOOL                    copyToChildForms, isFromAlert;
	NSMutableDictionary     *ccopyDownSrcChanged;

	NSInteger                     actionAfterSave;	// e.g. submit, itemization, or back.  Default to 0.

    EditFormHelper          *helper;
	DateTimePopoverVC		*pickerPopOverVC;

    // Customization in behavior
    BOOL                    noSaveConfirmationUponExit; // If set to true before viewDidLoad, do not use fake back button, thus no save confirmation upon hitting back button
    
    // cover stock back button
    UIControl               *backCover;
}

@property(nonatomic, strong) IBOutlet UITableView       *tableList;
@property(nonatomic, strong) GoogleRouteFinderHandler   *googleHandler;
@property(nonatomic, strong) NSMutableArray				*sections;
@property(nonatomic, strong) NSMutableDictionary		*sectionDataMap;
@property(nonatomic, strong) NSMutableDictionary		*sectionFieldsMap;
@property(nonatomic, strong) NSMutableArray				*allFields;
@property(nonatomic, strong) NSString					*formKey;
@property BOOL isDirty;
@property BOOL copyToChildForms;
@property BOOL isFromAlert;
@property(nonatomic, strong) NSMutableDictionary		*ccopyDownSrcChanged; //copy down source changed
@property(nonatomic, strong) EditFormHelper             *helper;
@property NSInteger actionAfterSave;
@property (nonatomic, strong) DateTimePopoverVC			*pickerPopOverVC;
@property BOOL noSaveConfirmationUponExit;

-(void)setupToolbar;
- (void) refreshView;

-(BOOL) canEdit;
-(BOOL) shouldAllowOfflineEditingwAtIndexPath:(NSIndexPath *)indexPath;
-(void) attemptedToEditWhileOffline;

-(BOOL) canUseListEditor:(FormFieldData*)field;
-(BOOL) canUseBoolCell:(FormFieldData*) field;
-(BOOL) canUseTextFieldEditor:(FormFieldData*)field;

-(void) showAttendeeEditor;
-(void) showTextAreaEditor:(FormFieldData*)field;
-(void)showExpenseTypeEditor:(FormFieldData*) field;
-(void)showDateEditor:(FormFieldData*) field;
-(void)prefetchForListEditor:(ListFieldEditVC*) lvc; // fetch list data
-(BOOL)shouldUseCacheOnlyForListEditor:(ListFieldEditVC*)lvc;
-(void)showListEditor:(FormFieldData*) field;
-(void)showTextFieldEditor:(FormFieldData*) field;
-(void)showCommentsEditor:(FormFieldData*) field;

-(NSArray*) getExcludeKeysForListEditor:(FormFieldData*) field; 

- (void)pickerDateTapped:(id)sender IndexPath:(NSIndexPath *)indexPath;

+(UIBarButtonItem*) makeNavButton:(NSString*)strKey enabled:(BOOL)state target:(id)tgt action:(NSString*) sel;

-(void)actionSave:(id)sender;
-(void)actionSaveImpl; // no check on dirty or saving in progress
-(void) saveForm:(BOOL)copyDownToChildForm;
-(NSString*) getCDMsg; // CopyDown msg - analyzer does not like the word 'copy'
-(BOOL) hasCopyDownChildren; // Override to determine whether to pop copy down msg
-(void) checkCopyDownForSave;
-(void) actionBack:(id)sender;
-(void)executeActionAfterSave;
-(void)clearActionAfterSave;
-(BOOL)isSaveConfirmDialog:(NSInteger) tag;
-(BOOL)isReceiptUploadAlertTag:(NSInteger) tag;
-(void) confirmToSave:(int) callerId;
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex;

-(BOOL) isFieldsSection:(NSInteger) section;
-(NSMutableArray*) getSectionData:(NSInteger) section;

// Editing methods
-(FormFieldData*) findEditingField:(NSString*) fId;
-(void) hideField:(FormFieldData*)fld;
-(void) hideFieldWithId:(NSString*) fId;
-(void) showField:(FormFieldData*)fld afterField:(FormFieldData*) srcFld;
-(void) refreshFields:(NSArray*) fields;
-(void) refreshField:(FormFieldData*) field;
-(FormFieldData*)findFieldWithIndexPath:(NSIndexPath*) indexPath;

-(double) getDoubleFromField:(NSString*) fldId;
-(NSDecimalNumber*) getDecimalNumberFromField:(NSString*) fldId;

-(void) initFields;
-(BOOL) validateFields:(BOOL*)missingReqFlds;
// Return a list of comments for comment editor
-(NSDictionary*) getComments;
// Merge existing changes to fields passed in.
-(NSMutableArray*) mergeFields:(NSDictionary*) fields withKeys:(NSArray*) keys;

// Editing Delegate methods
-(void) textUpdated:(NSObject*) context withValue:(NSString*) value;
-(void) dateSelected:(NSObject*) context withValue:(NSDate*) date;
-(void) boolUpdated:(NSObject*) context withValue:(BOOL) val;

// For iPad
-(void)closeMe:(id)sender;

extern int kActionAfterSaveDefault;
extern int kAlertViewVerifyCopyDown;
extern int kAlertViewConfirmSaveUponBack;
extern int kAlertViewMissingReqFlds;

-(void) doGoogleMessage;
-(void) handleGoogleLocation:(NSMutableDictionary *)dict didFail:(BOOL)didFail;

@end
