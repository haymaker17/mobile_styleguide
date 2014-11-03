//
//  FFFormFields.h
//  ConcurMobile
//
//  Created by laurent mery on 26/10/2014.
//  Copyright (c) 2014 concur. All rights reserved.
//

#import "CTEFormFields.h"
#import "CTEField.h"

#import "FFSection.h"
#import "FFFormProtocol.h"

/*
 
 Independant Form object To be used simply everywhere
 Models (CTEFormField and CTEField) are hosted in ConcurSDK
 
 To use it
 - create a child of this class to personalize your form
 - alloc/init your personalized form with your tableView (just add a grouped style to your empty tableView)
 - set form.delegate with you viewController (add protocol FFFormProtocol to your delegate/viewController)
 
 and use addForm method with Datas to draw/used your form
 
 - isValidAtIndexPath : to personnalize validation
 
 */



@interface FFFormFields : NSObject


/*
 * tableViewForm provide by init argument
 */
@property (nonatomic, retain) UITableView *tableViewForm;

/*
 * delegate : view controller
 */
@property (nonatomic, retain) id<FFFormProtocol> delegate;

/*
 * dictionnary provide when select editable value
 */
@property (nonatomic, copy) NSDictionary *currentUpdateContext;

/*
 * flag set to YES when forms have a change value
 */
@property (nonatomic, assign, readonly) BOOL isDirty;

/*
 * a form is a section, each time than you add a form, you add a new section
 */
@property (nonatomic, copy) NSMutableArray *sections;



/*
 * init the form with a style grouped tableview
 */
-(id)initWithTableView:(UITableView*)tableViewForm;


/*
 * create a section with form
 * manage a cache forms by formKey
 */
-(void)addForm:(NSString*)formName withFormKey:(NSString*)formKey andDatas:(id)datas;

/*
 * reload tableView
 */
-(void)refresh;


/*
 *
 */
-(void)refreshAtIndexPath:(NSIndexPath*)indexPath;

/*
 *
 */
-(void)setDirty;


/*
 * update an existing form with datas
 */
-(void)updateForm:(NSString*)formName withDatas:(id)datas;

/*
 * list fields
 * ordered and filtered (transformed CTEField's access proprety)
 * to be overrided by myForm to set specific filtered
 */
-(NSArray*)filteredFieldsInSection:(FFSection*)section;

/**
 * @protocol
 * delegate method
 * called after editing a value
 */
-(void)updateFormWithDictionnary:(NSDictionary*)dic;

/*
 * return formated label
 * if field is required, add an * at the end
 */
-(NSString*)labelAtIndexPath:(NSIndexPath*)indexPath;

/*
 * 
 */
-(CTEField*)fieldAtIndexPath:(NSIndexPath*)indexPath;

/*
 * get Value to set Cell's value
 * to be overrided by myForm to set specific value by context
 */
-(NSString*)valueAtIndexPath:(NSIndexPath*)indexPath;


/*
 *
 */
-(void)setValue:(NSString*)value oldValue:(NSString*)oldValue atIndexPath:(NSIndexPath*)indexPath;

/*
 * validate value
 * to be overrided by myForm to set specific validation
 */
-(BOOL)isValidAtIndexPath:(NSIndexPath*)indexPath;

@end