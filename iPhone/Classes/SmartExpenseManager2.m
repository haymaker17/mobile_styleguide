//
//  SmartExpenseManager2.m
//  ConcurMobile
//
//  Created by ernest cho on 8/16/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "SmartExpenseManager2.h"
#import "MCLogging.h"
#import "Localizer.h"
#import "MobileAlertView.h"
#import "EntityMobileEntry.h"
#import "EntitySplitSmartExpenses.h"

@interface SmartExpenseManager2()
@property (nonatomic, readwrite, strong) NSManagedObjectContext *context;
@end

@implementation SmartExpenseManager2

-(id) initWithContext:(NSManagedObjectContext *)context
{
    self = [super init];
    if (self) {
        self.context = context;
    }
    return self;
}

#pragma mark automerge methods
-(void) informUserThatExpensesWereMerged
{
    /*
     // confirm that we want this, it's seems over zealous about informing the user
     // it's marked in the spec as "Not in this release"
     UIAlertView *alert = [[MobileAlertView alloc]
     initWithTitle:[Localizer getLocalizedText:@"Expenses Combined"]
     message:[Localizer getLocalizedText:@"Matching transactions combined into a single expense"]
     delegate:nil
     cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
     otherButtonTitles:nil];
     [alert show];
     */
}

-(void) mergeSmartExpenses
{
    NSArray *smartExpenses = [self getSmartExpenses];
    for (int i=0; i<smartExpenses.count; i++) {
        id object = smartExpenses[i];
        if ([object isKindOfClass:[EntityMobileEntry class]]) {
            EntityMobileEntry *smartExpense = (EntityMobileEntry *)object;
            EntityMobileEntry *matchedExpense = [self getSmartExpenseMatch:smartExpense.smartExpenseMeKey];

            // user has indicated that this smart expense is invalid
            if (![self isSmartExpenseSplit:smartExpense]) {
                [self mergeExpense:smartExpense with:matchedExpense];
            }
        }
    }
}

-(void) mergeExpense:(EntityMobileEntry *)smartExpense with:(EntityMobileEntry *)matchedExpense
{
    // clone the smartExpense and copy over important stuff
    EntityMobileEntry *mergedExpense = (EntityMobileEntry *)[self clone:smartExpense inContext:self.context];

    mergedExpense.isMergedSmartExpense = [NSNumber numberWithBool:YES];

    // MOB-14608 use the Mobile Entry values for any editable field instead of the Credit Card one.
    if (matchedExpense.expKey.length != 0 && matchedExpense.expName != 0) {
        mergedExpense.expKey = matchedExpense.expKey;
        mergedExpense.expName = matchedExpense.expName;
    }
    if (matchedExpense.locationName.length != 0) {
        mergedExpense.locationName = matchedExpense.locationName;
    }
    if (matchedExpense.comment.length != 0) {
        mergedExpense.comment = matchedExpense.comment;
    }
    if (matchedExpense.hasReceipt.length != 0 && matchedExpense.receiptImageId.length != 0) {
        mergedExpense.hasReceipt = matchedExpense.hasReceipt;
        mergedExpense.receiptImageId = matchedExpense.receiptImageId;
    }

    // hide originals
    smartExpense.isHidden = [NSNumber numberWithBool:YES];
    matchedExpense.isHidden = [NSNumber numberWithBool:YES];

    // save changes
    NSError *error = nil;
    if ([self.context hasChanges] && ![self.context save:&error]) {
        NSLog(@"[SmartExpenseManager2 mergeSmartExpenses] Core Data Error: %@, %@", error, [error userInfo]);
    } else {
        [self informUserThatExpensesWereMerged];
    }
}

// does not handle loops in core data, but works otherwise
// http://stackoverflow.com/questions/2730832/how-can-i-duplicate-or-copy-a-core-data-managed-object
-(NSManagedObject *) clone:(NSManagedObject *)source inContext:(NSManagedObjectContext *)context
{
    NSString *entityName = [[source entity] name];

    //create new object in data store
    NSManagedObject *cloned = [NSEntityDescription insertNewObjectForEntityForName:entityName inManagedObjectContext:context];

    //loop through all attributes and assign then to the clone
    NSDictionary *attributes = [[NSEntityDescription entityForName:entityName inManagedObjectContext:context] attributesByName];

    for (NSString *attr in attributes) {
        [cloned setValue:[source valueForKey:attr] forKey:attr];
    }

    //Loop through all relationships, and clone them.
    NSDictionary *relationships = [[NSEntityDescription entityForName:entityName inManagedObjectContext:context] relationshipsByName];
    for (NSRelationshipDescription *rel in relationships) {
        NSString *keyName = [NSString stringWithFormat:@"%@",rel];
        //get a set of all objects in the relationship
        NSMutableSet *sourceSet = [source mutableSetValueForKey:keyName];
        NSMutableSet *clonedSet = [cloned mutableSetValueForKey:keyName];
        NSEnumerator *e = [sourceSet objectEnumerator];
        NSManagedObject *relatedObject;
        while ( relatedObject = [e nextObject]) {
            //Clone it, and add clone to set
            NSManagedObject *clonedRelatedObject = [self clone:relatedObject inContext:context];
            [clonedSet addObject:clonedRelatedObject];
        }

    }
    
    return cloned;
}


-(EntityMobileEntry *) getSmartExpenseMatch:(NSString *)key
{
    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    [request setEntity:[NSEntityDescription entityForName:@"EntityMobileEntry" inManagedObjectContext:self.context]];
    [request setPredicate:[NSPredicate predicateWithFormat:@"key == %@ && isHidden == NO", key]];
    NSArray *matches = [self.context executeFetchRequest:request error:nil];

    if (matches.count == 1) {
        // should only be one match
        return (EntityMobileEntry *)matches[0];
    } else {
        return nil;
    }
}

-(EntityMobileEntry *) getHiddenExpense:(NSString *)key
{
    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    [request setEntity:[NSEntityDescription entityForName:@"EntityMobileEntry" inManagedObjectContext:self.context]];
    [request setPredicate:[NSPredicate predicateWithFormat:@"key == %@ && isHidden == YES", key]];
    NSArray *matches = [self.context executeFetchRequest:request error:nil];

    if (matches.count == 1) {
        // should only be one match
        return (EntityMobileEntry *)matches[0];
    } else {
        NSLog(@"Error: [SmartExpenseManager2 getHiddenExpense] found more than one smart expense match.  Giving up!");
        return nil;
    }
}

-(NSArray *) getSmartExpenses
{
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"EntityMobileEntry" inManagedObjectContext:self.context];
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"((key != nil or cctKey != nil or pctKey != nil) && smartExpenseMeKey != nil && isHidden == NO)"];
    NSSortDescriptor *sortDate = [[NSSortDescriptor alloc] initWithKey:@"transactionDate" ascending:NO];

    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    [request setEntity:entityDescription];
    [request setPredicate:predicate];
    [request setSortDescriptors:[NSArray arrayWithObjects:sortDate, nil]];

    NSFetchedResultsController *frc = [[NSFetchedResultsController alloc] initWithFetchRequest:request managedObjectContext:self.context sectionNameKeyPath:nil cacheName:nil];

    NSError *error;
    if (![frc performFetch:&error])
    {
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"SmartExpenseManager2: fetchedResults %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];
        return nil;
    }

    return [frc fetchedObjects];
}

// Android maintains a list of smart expenses which the user has split on client, we'll do the same.
#pragma mark split methods
-(BOOL) isSmartExpenseSplit:(EntityMobileEntry *)smartExpense
{
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"EntitySplitSmartExpenses" inManagedObjectContext:self.context];

    // The key can be either the cctKey or the pctKey.  It appears to never be key.
    NSString *key = smartExpense.cctKey;
    if (key.length == 0) {
        key = smartExpense.pctKey;
    }
    if (key.length == 0) {
        return NO;
    }
    
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"(expenseKey == %@)", key];
    NSSortDescriptor *sortDate = [[NSSortDescriptor alloc] initWithKey:@"expenseKey" ascending:NO];

    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    [request setEntity:entityDescription];
    [request setPredicate:predicate];
    [request setSortDescriptors:[NSArray arrayWithObjects:sortDate, nil]];

    NSFetchedResultsController *frc = [[NSFetchedResultsController alloc] initWithFetchRequest:request managedObjectContext:self.context sectionNameKeyPath:nil cacheName:nil];

    NSError *error;
    if (![frc performFetch:&error])
    {
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"SmartExpenseManager2: fetchedResults %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];
        return NO;
    }

    NSArray *results = [frc fetchedObjects];
    if (results.count > 0) {
        //EntitySplitSmartExpenses *tmp = (EntitySplitSmartExpenses *)results[0];
        //NSLog(@"TEST: %@", tmp.expenseKey);
        return YES;
    }

    return NO;
}

-(void) splitSmartExpense:(EntityMobileEntry *)smartExpense
{
    if (![self isSmartExpenseSplit:smartExpense]) {
        NSDictionary *dict = @{@"From": @"Expense List"};
        [Flurry logEvent:@"SmartExpense: Unmatch" withParameters:dict];

        // The key can be either the cctKey or the pctKey.  It appears to never be key.
        NSString *key = smartExpense.cctKey;
        if (key.length == 0) {
            key = smartExpense.pctKey;
        }
        if (key.length == 0) {
            return;
        }

        // unhide the originals
        EntityMobileEntry *tmp = [self getHiddenExpense:key];
        tmp.isHidden = NO;
        tmp = [self getHiddenExpense:smartExpense.smartExpenseMeKey];
        tmp.isHidden = NO;

        // delete the merged entry from the list
        [self.context deleteObject:smartExpense];

        // Add to the list of split smart expenses
        EntitySplitSmartExpenses *newSplit = [NSEntityDescription insertNewObjectForEntityForName:@"EntitySplitSmartExpenses" inManagedObjectContext:self.context];
        newSplit.expenseKey = key;
        newSplit.matchedMobileEntryKey = smartExpense.smartExpenseMeKey;

        // save changes.  I wonder if this redraws the list.
        NSError *error = nil;
        if ([self.context hasChanges] && ![self.context save:&error]) {
            NSLog(@"[SmartExpenseManager2 splitSmartExpense] Core Data Error: %@, %@", error, [error userInfo]);
        }
    }
}

-(void) removeSplitSmartExpense:(EntityMobileEntry *)smartExpense
{
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"EntitySplitSmartExpenses" inManagedObjectContext:self.context];
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"(expenseKey == %@)", smartExpense.key];
    NSSortDescriptor *sortDate = [[NSSortDescriptor alloc] initWithKey:@"expenseKey" ascending:NO];

    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    [request setEntity:entityDescription];
    [request setPredicate:predicate];
    [request setSortDescriptors:[NSArray arrayWithObjects:sortDate, nil]];

    NSFetchedResultsController *frc = [[NSFetchedResultsController alloc] initWithFetchRequest:request managedObjectContext:self.context sectionNameKeyPath:nil cacheName:nil];

    NSError *error;
    if (![frc performFetch:&error])
    {
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"SmartExpenseManager2: fetchedResults %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];
        return;
    }

    // should have 0 or 1 result.
    NSArray *results = [frc fetchedObjects];
    for (int i=0; i<results.count; i++) {
        [self.context deleteObject:results[i]];
    }

    // save changes
    if ([self.context hasChanges] && ![self.context save:&error]) {
        NSLog(@"[SmartExpenseManager2 removeSplitSmartExpense] Core Data Error: %@, %@", error, [error userInfo]);
    }
}

@end
