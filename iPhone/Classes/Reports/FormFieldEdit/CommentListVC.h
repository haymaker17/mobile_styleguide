//
//  CommentListVC.h
//  ConcurMobile
//
//  Created by yiwen on 5/24/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "FormFieldData.h"
#import "FieldEditDelegate.h"

@interface CommentListVC : MobileViewController <
                        UITableViewDelegate
                        ,UITableViewDataSource
                        ,FieldEditDelegate>
{
    UITableView             *tableList;
	NSDictionary            *comments;
	NSMutableArray          *sortedComments;
    FormFieldData           *field;
    id<FieldEditDelegate>   __weak _delegate;
}

@property(nonatomic, strong) IBOutlet UITableView       *tableList;
@property(nonatomic, strong) NSDictionary				*comments;
@property(nonatomic, strong) NSMutableArray				*sortedComments;
@property(nonatomic, strong) FormFieldData				*field;
@property (weak, nonatomic) id<FieldEditDelegate>     delegate;

- (void)setSeedData:(NSDictionary*)comments field:(FormFieldData*)field delegate:(id<FieldEditDelegate>)del;
- (void)setupToolbar;
- (void)buttonAddPressed:(id) sender;
-(void) actionOnNoData:(id)sender;
-(NSString*) getViewIDKey;
-(void) insertCurrentComment:(FormFieldData*) fld;
- (BOOL) isLastCommentEditable;

@end
