//
//  FFFormController-private.h
//  ConcurMobile
//
//  Created by laurent mery on 13/12/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FFFormController.h"

#import "FFFormProtocol.h"
#import "FFFieldProtocol.h"
#import "FFCellDelegateProtocol.h"

#import "FFCells.h"



@interface FFFormController () <UITableViewDelegate, UITableViewDataSource, FFCellDelegateProtocol, FFFieldProtocol>

/*
 * delegate and tableViewForm provide by init
 */
@property (nonatomic, retain) id<FFFormProtocol> delegateVC;
@property (nonatomic, retain) UITableView *tableViewForm;

@property (nonatomic, assign) CGRect tableViewFormOriginalRect;


/*
 * one form = one section
 * each time than you add a form, you add a new section
 */
@property (nonatomic, copy) NSMutableArray *sections;






#pragma mark - at indexPath

-(NSString*)determineCellReuseIdentifierAtIndexPath:(NSIndexPath*)indexPath;


-(void)refreshAtIndexPath:(NSIndexPath*)indexPath;


-(FFField*)fieldAtIndexPath:(NSIndexPath*)indexPath;







@end