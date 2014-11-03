//
//  ReportEntryItemViewController.m
//  ConcurMobile
//
//  Created by yiwen on 5/19/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ReportEntryItemViewController.h"


@implementation ReportEntryItemViewController
@synthesize parentEntry, itemTbHelper;

-(NSString *)getViewIDKey
{
	return APPROVE_VIEW_ITEMIZATIONS;
}


- (void)setSeedData:(NSDictionary*)pBag
{
    [self setSeedData:pBag[@"REPORT"] entry:pBag[@"ENTRY"] item:pBag[@"ITEM"] role:pBag[@"ROLE"]];
    
}

- (void)setSeedData:(ReportData*)report entry:(EntryData*)thisEntry item:(EntryData*)thisItem role:(NSString*) curRole
{
    self.role = curRole;
    [self loadItem:thisItem withEntry:thisEntry withReport:report];

    // MOB-14815 force it to load taxforms!
    FormFieldData *transactionCurrency = [self findEditingField:@"TransactionCurrencyName"];
    FormFieldData *locName = [self findEditingField:@"LocName"];
    if (transactionCurrency != nil) {
        NSMutableDictionary *tmp = [[NSMutableDictionary alloc] init];
        [tmp setValue:transactionCurrency.liCode forKey:@"CrnCode"];
        [tmp setValue:transactionCurrency.liKey forKey:@"CrnKey"];

        locName.extraDisplayInfo = tmp;
    }
    [super fieldUpdated:locName];
}

// Replace both loadEntry and updateEntry
- (void) loadItem:(EntryData*) thisItem withEntry:(EntryData*) thisEntry withReport:(ReportData*) report
{
    self.parentEntry = thisEntry;
    self.itemTbHelper = [[ItemizedToolbarHelper alloc] init];
    [itemTbHelper setSeedData:self.parentEntry];
    [self loadEntry:thisItem withReport:report];
}

-(void)setupToolbar
{
    [super setupToolbar];
    if ([self canEdit] && 
        [ExSystem connectedToNetwork])
    {
        [itemTbHelper setupToolbar:self];
    }
}


#pragma mark - View lifecycle

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

@end
