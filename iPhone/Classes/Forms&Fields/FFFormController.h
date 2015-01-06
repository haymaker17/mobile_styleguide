//
//  FFFormController.h
//  ConcurMobile
//
//  Created by laurent mery on 26/10/2014.
//  Copyright (c) 2014 concur. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "FFSection.h"

#import "FFField.h"


/*
 
 Independant Form object To be used simply everywhere
 Models (CTEFormField and CTEField) are hosted in ConcurSDK
 
 To use it
 - create a child of this class to personalize your form/your datas
 - alloc/init with your tableView (just set a grouped style to an empty tableView)
 - set form.delegate with you viewController (add protocol FFFormProtocol to your delegate/viewController)
 
 and use addForm method with Datas to draw/used your form
 
 - isValidAtIndexPath : to personnalize validation
 
 */



@interface FFFormController : NSObject



/*
 * this boolean is on to inform view controller (our delegate)
 * that we need to do some action after an external edition
 * //TODO: set a listener on delegate.activeView to do the job
 */
@property (nonatomic, assign) BOOL isPendingExternalEdited;


/*
 * init your tableview with a style grouped tableview
 *
 * !important !
 * tableView need to be configured as
 *  - grouped style
 *  - row height
 */
-(id)initWithTableView:(UITableView*)tableViewForm andDelegate:(id)delegate;

/*
 * create a section=form
 * manage a cache forms by formID
 */
-(void)addForm:(NSString*)formName withFormID:(NSString*)formID isEditable:(BOOL)isEditable;

-(CTEDataTypes*)dataTypeForFfFieldLight:(FFFieldLight*)ffFieldLight;
-(NSArray*)fieldsOrderedInSection:(FFSection*)section;


#pragma mark - tableview

-(void)refreshAll;
-(void)refreshCurrentEditedCell;
-(CGFloat)heightRowConfigTableViewForm;


#pragma mark - cell

//#protocol FFFormProtocol
-(NSString*)iconLabelForField:(FFField*)field;


#pragma mark - form methods

//#protocol FFCellValidationProtocol
-(NSArray*)errorsOnValidateField:(FFField*)field;
-(NSString*)accessForField:(FFField*)field;

-(BOOL)isDirtyForm:(NSString*)formName;
-(BOOL)isDirtyAllForms;
-(void)resetForm:(NSString*)formName;
-(void)resetAllForms;

@end