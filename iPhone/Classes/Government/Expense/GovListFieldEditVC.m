//
//  GovListFieldEditVC.m
//  ConcurMobile
//
//  Created by Shifan Wu on 1/21/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovListFieldEditVC.h"
#import "GovExpListFieldSearchData.h"

@interface GovListFieldEditVC ()

@end

@implementation GovListFieldEditVC
@synthesize formAttributes;

-(void)viewDidLoad
{
    [super viewDidLoad];
    
    NSMutableArray *listChoices = (self.field.listChoices == nil ? [[NSMutableArray alloc] init] : [NSMutableArray arrayWithArray:self.field.listChoices]);
    
    self.sectionKeys = [[NSMutableArray alloc] initWithObjects: @"ATTENDEE_TYPES", nil];
    self.sections = [[NSMutableDictionary alloc] initWithObjectsAndKeys: listChoices, @"ATTENDEE_TYPES", nil];
    

    if ([self.field isSearchable])
        [self hideLoadingView];
    else
        [self hideSearchBar];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [self.searchHistory setObject:field.listChoices forKey:@""];
}

-(void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:GOV_EXP_LIST_FIELD_SEARCH_DATA])
    {
        if (msg.responseCode == 200 || msg.isCache)
		{
			GovExpListFieldSearchData*data = (GovExpListFieldSearchData*) msg.responder;
            
            [self hideLoadingView];
			self.searchResults = [[NSArray alloc] initWithArray:[super filterAndaddNoneItem:data.field.searchableListChoices]];
            
            [sections setObject:searchResults forKey:@"All Items"];
            if(![sectionKeys containsObject:@"All Items"])
                [sectionKeys addObject:@"All Items"];
            
			if ((data.field.searchableListChoices == nil || data.field.searchableListChoices.count < 500)
				&& (data.query == nil || [data.query isEqualToString:@""]) // always add all items to sections.
				&& !data.isMru && ![data.fieldId isEqualToString:@"LnKey"])
			{
				self.fullResults = self.searchResults;
 			}
            
			[searchHistory setObject:self.searchResults forKey:(data.query==nil?@"":data.query)];
            
            if ([self isViewLoaded])
			{
				[self.resultsTableView reloadData];
			}
        }
    }
}

-(void) getServerSearchResultWithText:(NSString*)sText
{
    NSString *docType = [self.formAttributes objectForKey:@"docType"];
    NSString *expDescrip = [self.formAttributes objectForKey:@"description"];
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: field, @"FIELD", sText, @"QUERY", docType, @"DOCTYPE", expDescrip, @"EXPTYPE", nil];
    
    [[ExSystem sharedInstance].msgControl createMsg:GOV_EXP_LIST_FIELD_SEARCH_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}
@end
