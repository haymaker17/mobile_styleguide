//
//  FormVCBaseInline.h
//  ConcurMobile
//
//  Created by Shifan Wu on 10/15/13.
//
//  This base class maintains the following
//  - list of fields (FormFieldData) to be edited
//  - isDirty flag
//  - Save button on the right on top bar.  Disable it if nothing to edit.
//  - Invoke in-line editors, according to field data type.
//  - Provide call backs after finishing editing of fields
//
//  This class assumes that the fields will be displayed in tableList (UITableView), and the table might contain cells other than fields.
//  This class supports UI elements other than the tableList.
//
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MobileTableViewController.h"
#import "BoolEditDelegate.h"
#import "FieldEditDelegate.h"
#import "EditFormHelper.h"
#import "FormFieldData.h"

#import "CreateExpenseDS.h"

@protocol FormVCBaseInlineDelegate;
@protocol FormInlineDelegate;

@interface FormVCBaseInline : MobileTableViewController <CreateExpenseDSDelegate, BoolEditDelegate, FieldEditDelegate>
{
    CreateExpenseDS     *createExpDS;
    
    id<FormVCBaseInlineDelegate>    __weak delegate;
}

@property (strong, nonatomic) IBOutlet UITableView *tableList;
@property (strong, nonatomic) IBOutlet CreateExpenseDS *createExpDS;
@property (nonatomic, weak) id<FormVCBaseInlineDelegate> delegate;
@property (nonatomic, weak) IBOutlet id<FormInlineDelegate> formDataSource;



@property (nonatomic, strong) NSArray *dataArray;
@property (nonatomic, strong) NSDateFormatter *dateFormatter;

// keep track which indexPath points to the cell with UIDatePicker
@property (nonatomic, strong) NSIndexPath *datePickerIndexPath;

@property (assign) NSInteger pickerCellRowHeight;

@property (nonatomic, strong) IBOutlet UIDatePicker *pickerView;

// this button appears only when the date picker is shown (iOS 6.1.x or earlier)
@property (nonatomic, strong) IBOutlet UIBarButtonItem *doneButton;

// In-line date picker
- (void)updateDatePicker;
- (BOOL)hasInlineDatePicker;
- (void)localeChanged:(NSNotification *)notif;
- (BOOL)hasPickerForIndexPath:(NSIndexPath *)indexPath;
- (BOOL)indexPathHasPicker:(NSIndexPath *)indexPath;
- (BOOL)formFieldHasDate:(FormFieldData *)fld;

// In-line text entry

// Actions
- (IBAction)dateAction:(id)sender;
- (IBAction)doneAction:(id)sender;

// FormViewContrllerBase.h copy over
@property(nonatomic, strong) NSMutableArray				*sections;
@property(nonatomic, strong) NSMutableDictionary		*sectionDataMap;
@property(nonatomic, strong) NSMutableDictionary		*sectionFieldsMap;
@property(nonatomic, strong) NSMutableArray				*allFields;
@property(nonatomic, strong) NSString					*formKey;

@property(nonatomic, strong) NSMutableDictionary		*ccopyDownSrcChanged; //copy down source changed
@property(nonatomic, strong) EditFormHelper             *helper;

@property int actionAfterSave;
@property BOOL isDirty;
@property BOOL copyToChildForms;
@property BOOL isFromAlert;

-(BOOL) canEdit;
-(void)actionSaveImpl;
-(void)clearActionAfterSave;
-(BOOL)isSaveConfirmDialog:(int) tag;
-(BOOL)isReceiptUploadAlertTag:(int) tag;
-(FormFieldData*) findEditingField:(NSString*) fldString;
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex;

// In line editing with organized structure

@end

@protocol FormVCBaseInlineDelegate <NSObject>

- (NSInteger) numberOfFields;
- (void)tableView:(UITableView *)tableView didSelectFieldAtIndexPath:(NSIndexPath *)indexPath;


@end

@protocol FormInlineDelegate <NSObject>

- (NSInteger) numberOfFields;
- (UITableViewCell*) tableView:(UITableView*)tableView fieldForIndex:(NSInteger)index;
- (void)setFormTableView:(UITableView*)tableView;

- (void)tableView:(UITableView *)tableView didSelectFieldAtIndex:(NSInteger *)index;


@end
