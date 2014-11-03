//
//  CommentListVC.m
//  ConcurMobile
//
//  Created by yiwen on 5/24/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "CommentListVC.h"
#import "TextAreaEditVC.h"
#import "CommentData.h"
#import "DateTimeFormatter.h"
#import "CommentCell.h"
#import "FormatUtils.h"
#import "ExSystem.h"

@implementation CommentListVC
@synthesize tableList, comments, sortedComments, field;
@synthesize delegate = _delegate;

-(NSString*) getViewIDKey
{
    return @"COMMENT_LIST";
}

- (void)setSeedData:(NSDictionary*)cDict field:(FormFieldData*)fld delegate:(id<FieldEditDelegate>)del
{
    self.comments = cDict;
    self.field = fld;
    self.delegate = del;
    // Sort comments by date in desc order
    NSMutableArray* cList = [NSMutableArray arrayWithCapacity:[cDict count]]; 
    for (NSString* cKey in self.comments)
    {
        CommentData* c = (self.comments)[cKey];
        [cList addObject:c];
    }
    
    NSSortDescriptor *dateDescriptor = [[NSSortDescriptor alloc] initWithKey:@"creationDate" ascending:NO];
    NSArray *sortDescriptors = @[dateDescriptor];
    NSArray *sortedArray = [cList sortedArrayUsingDescriptors:sortDescriptors];

  
    self.sortedComments = [NSMutableArray arrayWithCapacity:[cDict count]];
    for (CommentData* c in sortedArray)
    {
        [self.sortedComments addObject:c];
    }
    
    if (fld.extraDisplayInfo == nil && sortedComments != nil && [sortedComments count]>0)
    {
        CommentData* lastComment = (CommentData*)sortedComments[0];
        if ([fld.fieldValue isEqualToString:lastComment.comment])
            fld.extraDisplayInfo = lastComment.commentKey;
    }
    
    if ([fld.fieldValue lengthIgnoreWhitespace] && ![self isLastCommentEditable])
        [self insertCurrentComment:fld];

}


#pragma mark - View lifecycle
- (void) refreshView
{
    if ([self isViewLoaded])
	{
        if (sortedComments == nil || [sortedComments count] ==0) 
        {//show we gots no data view
            [self showNoDataView:self];
        }
        else
        {//refresh from the server, after an initial no show...
            [self hideNoDataView];
        }
        [self.tableList reloadData];

		[self setupToolbar];
        [self hideWaitView];        
	}
}

- (void)viewDidAppear:(BOOL)animated
{
    [self refreshView];    
    [super viewDidAppear:animated];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.title = [Localizer getLocalizedText:@"Comments"];
    
    if ([UIDevice isPad])
        self.contentSizeForViewInPopover = CGSizeMake(500.0, 400.0);

    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

#pragma mark -
#pragma mark NoData Delegate Methods 
-(void) actionOnNoData:(id)sender
{
    [self buttonAddPressed:sender];
}

#pragma mark -
#pragma mark NoData Delegate Methods 
- (BOOL) isCurrentComment:(CommentData*) comment
{
    if (field.access == nil || [field.access isEqualToString:@"RW"])
        return [comment.isLatest isEqualToString:@"Y"] && (comment.commentKey ==  nil || [comment.commentKey isEqualToString:(NSString*)field.extraDisplayInfo]);
    
    return false;
}

- (BOOL) isLastCommentEditable
{
    return self.sortedComments !=nil && [self.sortedComments count] > 0 &&
    [self isCurrentComment:(self.sortedComments)[0]];
}

-(void) showTextAreaEditor
{
	TextAreaEditVC *vc = [[TextAreaEditVC alloc] initWithNibName:@"TextAreaEditView" bundle:nil];
	
	vc.field = self.field;
	vc.delegate = self;
	[self.navigationController pushViewController:vc animated:YES];
}

-(BOOL)canShowActionOnNoData
{
    return self.field != nil && (self.field.access == nil || [self.field.access isEqualToString:@"RW"]);
}

-(void)buttonAddPressed:(id) sender
{
    [self showTextAreaEditor];
}

-(void)setupToolbar
{
    // Can edit comment, but last one is not editable, show add btn
    if ((field.access == nil || [field.access isEqualToString:@"RW"]) && 
         ![self isLastCommentEditable] &&
        [ExSystem connectedToNetwork])
    {
		UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddPressed:)];
		
        self.navigationItem.rightBarButtonItem = nil;
        [self.navigationItem setRightBarButtonItem:btnAdd animated:NO];
	}
    else
    {
        self.navigationItem.rightBarButtonItem = nil;        
        [self.navigationItem setRightBarButtonItem:nil animated:NO];
    }
}

#pragma mark -
#pragma mark FieldEditDelegate Methods
-(void) fieldCanceled:(FormFieldData*) field
{
}

-(void) insertCurrentComment:(FormFieldData*) fld
{
    CommentData* c = [[CommentData alloc] init];
    c.comment = fld.fieldValue;
    c.creationDate = [CCDateUtilities formatDateToISO8601DateTimeInString:[NSDate date]];
    c.commentBy = [ExSystem sharedInstance].userName;
    c.isLatest = @"Y";
    [self.sortedComments insertObject:c atIndex:0];
}

-(void) fieldUpdated:(FormFieldData*) fld
{
    // Adjust isLatest, if add comment, hide the add comment button
    [self.delegate fieldUpdated:fld];
    if (![self isLastCommentEditable])
    {
        [self insertCurrentComment:fld];
    }
    else
    {
        // Update last comment
        CommentData* c = (self.sortedComments)[0];
        c.comment = fld.fieldValue;
    }
    [self refreshView];
}

#pragma mark -
#pragma mark UITableViewDelegate Methods
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
    NSInteger section = [indexPath section];
	CommentData* c = sortedComments[section];

    CommentCell *cell = (CommentCell *)[tableView dequeueReusableCellWithIdentifier:@"CommentCell"];
    if (cell == nil)  
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"CommentCell" owner:self options:nil];
        for (id oneObject in nib)
        {
            if ([oneObject isKindOfClass:[CommentCell class]])
            {
                cell = (CommentCell *)oneObject;
                break;
            }
        }
    }
    
    [cell resetCellContent:c.comment commentedBy:c.commentBy];
    
    int commentWidth = 265;
//    if([ExSystem isLandscape])
//        commentWidth = 425;
    CGFloat height =  [FormatUtils getTextFieldHeight:commentWidth Text:c.comment FontSize:14.0f];
    
    cell.lblComment.frame = CGRectMake(10, 20, commentWidth, height);

    if (section == 0 && [self isCurrentComment:c])
        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    else
        cell.accessoryType = UITableViewCellAccessoryNone;

	return cell;
	
}	

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return sortedComments == nil?0:[sortedComments count];
}

- (NSString *)tableView:(UITableView *)tblView 
titleForFooterInSection:(NSInteger)section
{
    CommentData* comment = sortedComments[section];
    
	return [CCDateUtilities formatDateToMMMddYYYFromString:comment.creationDate];
}

// Customize the number of rows in the table view.
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section 
{
    return 1;
}


-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    NSInteger section = [newIndexPath section];
	if (section == 0 && [self isCurrentComment:sortedComments[0]])
    {
        [self showTextAreaEditor];
    }
    
}


- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSInteger section = [indexPath section];
	CommentData* c = sortedComments[section];

    int commentWidth = 265;
    //    if([ExSystem isLandscape])
    //        commentWidth = 425;
    CGFloat height =  [FormatUtils getTextFieldHeight:commentWidth Text:c.comment FontSize:14.0f];

	return height + 25; // 20 as y and 5 as bottom padding
}



@end
